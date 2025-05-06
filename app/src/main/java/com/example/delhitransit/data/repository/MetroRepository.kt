package com.example.delhitransit.data.repository

import android.content.Context
import android.util.Log
import com.example.delhitransit.data.local.entity.MetroLineEntity
import com.example.delhitransit.data.local.StationDao
import com.example.delhitransit.data.local.dao.MetroLineDao
import com.example.delhitransit.data.local.entity.StationEntity
import com.example.delhitransit.data.model.MetroLine
import com.example.delhitransit.data.model.Route
import com.example.delhitransit.data.model.Station
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MetroRepository @Inject constructor(
    private val stationDao: StationDao,
    private val metroLineDao: MetroLineDao,
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val graph = mutableMapOf<Station, MutableSet<Station>>()
    private var allStationsList = mutableListOf<Station>()
    private val stationMap = mutableMapOf<String, Station>() // For quick lookup by name

    init {
        repositoryScope.launch {
            loadStationsFromDatabase()
            buildGraph()
        }
    }

    val allStationsFlow: Flow<List<Station>>
        get() = stationDao.getAllStations()
            .map { entities ->
                entities.map { it.toStation() }
            }
            .onEach { stations ->
                updateInMemoryData(stations)
            }

    val allLinesFlow: Flow<List<MetroLine>> = metroLineDao.getAllLines()
        .map { entities -> entities.map { it.toMetroLine() } }

    private fun updateInMemoryData(stations: List<Station>) {
        allStationsList = stations.toMutableList()
        stationMap.clear()
        stations.forEach { station ->
            stationMap[station.name.lowercase()] = station
        }
        buildGraph()
    }

    private suspend fun loadStationsFromDatabase() {
        withContext(ioDispatcher) {
            val stations = stationDao.getAllStations().first().map { it.toStation() }
            allStationsList = stations.toMutableList()

            stationMap.clear()
            stations.forEach { station ->
                stationMap[station.name.lowercase()] = station
            }
        }
    }

    private fun buildGraph() {
        graph.clear()

        val lineStations = allStationsList.groupBy { it.line }

        lineStations.forEach { (_, stations) ->
            if (stations.size > 1) {
                val sortedStations = stations.sortedBy { it.stationId }

                for (i in 0 until sortedStations.size - 1) {
                    addEdge(sortedStations[i], sortedStations[i + 1])
                }
            }
        }

        val stationsByName = allStationsList.groupBy { it.name.lowercase() }
        stationsByName.values.filter { it.size > 1 }.forEach { sameNameStations ->
            for (i in 0 until sameNameStations.size) {
                for (j in i + 1 until sameNameStations.size) {
                    addEdge(sameNameStations[i], sameNameStations[j])
                }
            }
        }

        Log.d("MetroRepository", "Built graph with ${graph.size} nodes")
    }

    private fun addEdge(station1: Station, station2: Station) {
        graph.getOrPut(station1) { mutableSetOf() }.add(station2)
        graph.getOrPut(station2) { mutableSetOf() }.add(station1)
    }



    suspend fun getStation(stationName: String): Station? = withContext(ioDispatcher) {
        val normalizedName = stationName.lowercase().trim()

        // Try to find in memory cache first
        return@withContext stationMap[normalizedName] ?: run {
            // If not in cache, query the database
            val entity = stationDao.getStationByName(normalizedName)
            entity?.toStation()?.also { station ->
                // Update cache
                stationMap[normalizedName] = station
            }
        }
    }

    suspend fun getRoute(sourceStation: String, destinationStation: String): Route {
        return withContext(ioDispatcher) {
            if (allStationsList.isEmpty()) {
                loadStationsFromDatabase()
                buildGraph()
            }

            val source = getStation(sourceStation)
            val destination = getStation(destinationStation)

            if (source == null || destination == null) {
                throw IllegalArgumentException("Invalid station names: $sourceStation or $destinationStation")
            }

            val path = computeShortestRoute(source, destination)

            Route(
                source = source,
                destination = destination,
                path = path,
                totalStations = path.size - 1,
                interchangeCount = calculateInterchangeCount(path)
            )
        }
    }

    private fun computeShortestRoute(start: Station, end: Station): List<Station> {
        if (start.name.equals(end.name, ignoreCase = true)) return listOf(start)

        val explored = mutableSetOf<Station>()
        val shortestDistances = mutableMapOf<Station, Int>()
        val pathTrace = mutableMapOf<Station, Station>()

        val stationComparator = Comparator<Station> { a, b ->
            (shortestDistances[a] ?: Int.MAX_VALUE).compareTo(shortestDistances[b] ?: Int.MAX_VALUE)
        }

        val priorityQueue = PriorityQueue(stationComparator)

        allStationsList.forEach { station ->
            shortestDistances[station] = Int.MAX_VALUE
        }

        shortestDistances[start] = 0
        priorityQueue.add(start)

        while (priorityQueue.isNotEmpty()) {
            val currentStation = priorityQueue.poll() ?: continue

            if (currentStation == end) break
            if (currentStation in explored) continue

            explored.add(currentStation)

            val adjacentStations = graph[currentStation] ?: continue
            for (nextStation in adjacentStations) {
                if (nextStation !in explored) {
                    val transitionCost = if (currentStation.line == nextStation.line) 1 else 4
                    val totalDistance = (shortestDistances[currentStation] ?: Int.MAX_VALUE) + transitionCost

                    if (totalDistance < (shortestDistances[nextStation] ?: Int.MAX_VALUE)) {
                        shortestDistances[nextStation] = totalDistance
                        pathTrace[nextStation] = currentStation
                        priorityQueue.add(nextStation)
                    }
                }
            }
        }

        // Backtrack to build final path
        val resultPath = mutableListOf<Station>()
        if (pathTrace.containsKey(end)) {
            var step: Station? = end
            while (step != null) {
                resultPath.add(0, step)
                if (step == start) break
                step = pathTrace[step]
            }
        } else {
            resultPath.add(start)
            if (start != end) resultPath.add(end)
        }

        return resultPath
    }

    private fun calculateInterchangeCount(path: List<Station>): Int {
        var count = 0
        for (i in 1 until path.size) {
            if (path[i].line != path[i - 1].line) {
                count++
            }
        }
        return count
    }

    // For searching stations
    suspend fun searchStation(query: String): List<Station> {
        return withContext(ioDispatcher) {
            val normalizedQuery = query.lowercase().trim()
            if (normalizedQuery.isEmpty()) {
                return@withContext emptyList()
            }

            // Query database
            val results = stationDao.searchStations(normalizedQuery).first()
            return@withContext results.map { it.toStation() }
        }
    }

    // Initialize database from JSON
    suspend fun initializeDatabaseFromJson() {
        withContext(ioDispatcher) {
            // Check if database is already populated
            val stationCount = stationDao.getStationCount()

            if (stationCount == 0) {
                try {
                    Log.d("MetroRepository", "Initializing database from JSON")
                    val jsonContent = context.assets.open("DMRC_STATIONS.json")
                        .bufferedReader().use { it.readText() }

                    val gson = Gson()
                    val linesMap = gson.fromJson(jsonContent,
                        object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type)
                            as Map<String, List<Map<String, Any>>>

                    val stationEntities = mutableListOf<StationEntity>()
                    val lineEntities = mutableListOf<MetroLineEntity>()

                    linesMap.forEach { (lineName, stationsList) ->
                        // Add line
                        lineEntities.add(
                            MetroLineEntity(
                                name = lineName,
                                color = getLineColor(lineName),
                                totalStations = stationsList.size
                            )
                        )

                        // Add stations
                        stationsList.forEach { stationData ->
                            try {
                                val name = stationData["name"] as? String
                                val stationId = (stationData["stationId"] as? Double)?.toInt()
                                val lat = stationData["lat"] as? Double
                                val long = stationData["long"] as? Double

                                if (name != null && stationId != null && lat != null && long != null) {
                                    stationEntities.add(
                                        StationEntity(
                                            name = name.trim(),
                                            line = lineName,
                                            stationId = stationId,
                                            latitude = lat,
                                            longitude = long
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("MetroRepository", "Error parsing station: ${e.message}")
                            }
                        }
                    }

                    // Insert into database
                    metroLineDao.insertLines(lineEntities)
                    stationDao.insertStations(stationEntities)

                    Log.d("MetroRepository", "Database initialized with ${stationEntities.size} stations and ${lineEntities.size} lines")
                } catch (e: Exception) {
                    Log.e("MetroRepository", "Error initializing database: ${e.message}")
                }
            } else {
                Log.d("MetroRepository", "Database already contains $stationCount stations, skipping initialization")
            }
        }
    }

    // Helper methods to convert between entity and domain models
    private fun StationEntity.toStation(): Station =
        Station(
            name = name,
            line = line,
            stationId = stationId,
            latitude = latitude,
            longitude = longitude
        )

    private fun MetroLineEntity.toMetroLine(): MetroLine =
        MetroLine(
            name = name,
            stations = emptyList()
        )

    private fun getLineColor(line: String): String {
        return when (line.lowercase()) {
            "yellow" -> "#FFD700"
            "blue" -> "#1976D2"
            "red" -> "#D32F2F"
            "green" -> "#4CAF50"
            "violet" -> "#8E24AA"
            "orange" -> "#FF9800"
            "magenta" -> "#E91E63"
            "pink" -> "#FF80AB"
            "aqua" -> "#00BCD4"
            "grey" -> "#757575"
            "rapid" -> "#4682B4"
            "greenbranch" -> "#388E3C"
            "bluebranch" -> "#1565C0"
            "pinkbranch" -> "#EC407A"
            else -> "#757575"
        }
    }
}