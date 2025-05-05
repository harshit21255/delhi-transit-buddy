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
    // Create a repository-scoped coroutine scope with SupervisorJob()
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // In-memory graph for path finding
    private val graph = mutableMapOf<Station, MutableSet<Station>>()
    private var allStationsList = mutableListOf<Station>()
    private val stationMap = mutableMapOf<String, Station>() // For quick lookup by name

    init {
        // Initialize data on repository creation with proper scope
        repositoryScope.launch {
            loadStationsFromDatabase()
            buildGraph()
        }
    }

    // Flow to observe all stations from Room database
    val allStationsFlow: Flow<List<Station>>
        get() = stationDao.getAllStations()
            .map { entities ->
                entities.map { it.toStation() }
            }
            .onEach { stations ->
                // Update in-memory data structures when database changes
                updateInMemoryData(stations)
            }

    // Flow to observe all lines from Room database
    val allLinesFlow: Flow<List<MetroLine>> = metroLineDao.getAllLines()
        .map { entities -> entities.map { it.toMetroLine() } }

    // Update in-memory cache of stations
    private fun updateInMemoryData(stations: List<Station>) {
        allStationsList = stations.toMutableList()
        stationMap.clear()
        stations.forEach { station ->
            stationMap[station.name.lowercase()] = station
        }
        buildGraph()
    }

    // Load stations into memory from database
    private suspend fun loadStationsFromDatabase() {
        withContext(ioDispatcher) {
            val stations = stationDao.getAllStations().first().map { it.toStation() }
            allStationsList = stations.toMutableList()

            // Create lookup map
            stationMap.clear()
            stations.forEach { station ->
                stationMap[station.name.lowercase()] = station
            }
        }
    }

    // Build graph for path finding
    private fun buildGraph() {
        graph.clear()

        // Group stations by line
        val lineStations = allStationsList.groupBy { it.line }

        // Connect stations on the same line based on stationId sequence
        lineStations.forEach { (_, stations) ->
            if (stations.size > 1) {
                // Sort by stationId to ensure correct sequence
                val sortedStations = stations.sortedBy { it.stationId }

                // Connect adjacent stations
                for (i in 0 until sortedStations.size - 1) {
                    addEdge(sortedStations[i], sortedStations[i + 1])
                }
            }
        }

        // Connect interchange stations (stations with same name but different lines)
        val stationsByName = allStationsList.groupBy { it.name.lowercase() }
        stationsByName.values.filter { it.size > 1 }.forEach { sameNameStations ->
            // Connect all stations with the same name (interchanges)
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

    suspend fun getAllStations(): List<Station> = withContext(ioDispatcher) {
        if (allStationsList.isEmpty()) {
            // Load from database if memory cache is empty
            loadStationsFromDatabase()
        }
        return@withContext allStationsList
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
            // Make sure we have latest data
            if (allStationsList.isEmpty()) {
                loadStationsFromDatabase()
                buildGraph()
            }

            val source = getStation(sourceStation)
            val destination = getStation(destinationStation)

            if (source == null || destination == null) {
                throw IllegalArgumentException("Invalid station names: $sourceStation or $destinationStation")
            }

            val path = findShortestPath(source, destination)

            Route(
                source = source,
                destination = destination,
                path = path,
                totalStations = path.size - 1, // Number of stations excluding source
                interchangeCount = calculateInterchangeCount(path)
            )
        }
    }

    private fun findShortestPath(source: Station, destination: Station): List<Station> {
        // If source and destination are the same
        if (source.name.equals(destination.name, ignoreCase = true)) {
            return listOf(source)
        }

        // Initialize Dijkstra algorithm with priority queue
        val visited = mutableSetOf<Station>()
        val distances = mutableMapOf<Station, Int>()
        val previousStation = mutableMapOf<Station, Station>()

        // Custom comparator for priority queue based on distance
        val comparator = Comparator<Station> { s1, s2 ->
            (distances[s1] ?: Int.MAX_VALUE).compareTo(distances[s2] ?: Int.MAX_VALUE)
        }

        val queue = PriorityQueue(comparator)

        // Initialize all distances as infinite
        allStationsList.forEach { station ->
            distances[station] = Int.MAX_VALUE
        }

        // Distance to source is 0
        distances[source] = 0
        queue.add(source)

        // Main Dijkstra algorithm loop
        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue // Safe null check

            // If we reached the destination, we can stop
            if (current == destination) {
                break
            }

            // Skip if already visited
            if (current in visited) {
                continue
            }

            visited.add(current)

            // Process all neighbors
            val neighbors = graph[current] ?: continue // Safe null check
            for (neighbor in neighbors) {
                if (neighbor !in visited) {
                    // Calculate new distance
                    // 1 unit for same line stations, 3 units penalty for interchange
                    val edgeWeight = if (current.line == neighbor.line) 1 else 3
                    val currentDistance = distances[current] ?: Int.MAX_VALUE
                    val newDistance = currentDistance + edgeWeight

                    // If we found a better path
                    val neighborDistance = distances[neighbor] ?: Int.MAX_VALUE
                    if (newDistance < neighborDistance) {
                        distances[neighbor] = newDistance
                        previousStation[neighbor] = current
                        queue.add(neighbor)
                    }
                }
            }
        }

        // Reconstruct path
        val path = mutableListOf<Station>()

        // Check if destination is reachable
        if (previousStation.containsKey(destination)) {
            var current: Station? = destination
            while (current != null) {
                path.add(0, current)
                if (current == source) {
                    break
                }
                current = previousStation[current]
            }
        } else {
            // No path found, return just source and destination
            path.add(source)
            if (source != destination) {
                path.add(destination)
            }
        }

        return path
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