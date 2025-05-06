// BusRepository.kt (new file)
package com.example.delhitransit.data.repository

import android.content.Context
import android.util.Log
import com.example.delhitransit.data.local.dao.*
import com.example.delhitransit.data.local.entity.*
import com.example.delhitransit.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.PriorityQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusRepository @Inject constructor(
    private val busAgencyDao: BusAgencyDao,
    private val busRouteDao: BusRouteDao,
    private val busStopDao: BusStopDao,
    private val busTripDao: BusTripDao,
    private val busStopSequenceDao: BusStopSequenceDao,
    private val stopTimeDao: StopTimeDao,
    private val busRouteTripDao: BusRouteTripDao,
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allStopsFlow: Flow<List<BusStop>> = busStopDao.getAllStops()
        .map { entities -> entities.map { it.toBusStop() } }

    val allRoutesFlow: Flow<List<BusRoute>> = busRouteDao.getAllRoutes()
        .map { entities -> entities.map { it.toBusRoute() } }

    suspend fun initializeDatabaseFromGtfs() {
        withContext(ioDispatcher) {
            try {
                // Check if database is already populated
                val routeCount = busRouteDao.getRouteCount()
                val stopCount = busStopDao.getStopCount()

                Log.d("BusRepository", "Database has $routeCount routes and $stopCount stops")

                // Only initialize if needed
                Log.d("BusRepository", "Initializing database from GTFS files")

                // Load entities in appropriate order
                loadAgenciesFromCsv().also {
                    busAgencyDao.insertAgencies(it)
                    Log.d("BusRepository", "Loaded ${it.size} agencies")
                }

                loadRoutesFromCsv().also {
                    busRouteDao.insertRoutes(it)
                    Log.d("BusRepository", "Loaded ${it.size} routes")
                }

                loadStopsFromCsv().also {
                    busStopDao.insertStops(it)
                    Log.d("BusRepository", "Loaded ${it.size} stops")
                }

                loadTripsFromCsv().also {
                    busTripDao.insertTrips(it)
                    Log.d("BusRepository", "Loaded ${it.size} trips")
                }

                // Load stop times with optimized method
                loadStopTimesFromCsv()

                // Generate combined data after all entities are loaded
                generateCombinedData()

                Log.d("BusRepository", "Database initialization completed")

            } catch (e: Exception) {
                Log.e("BusRepository", "Error initializing database: ${e.message}", e)
            }
        }
    }

    private fun loadStopsFromCsv(): List<BusStopEntity> {
        val stops = mutableListOf<BusStopEntity>()
        val inputStream = context.assets.open("GTFS/stops.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Parse header to get column indices
        val header = reader.readLine()
        val headerColumns = header.split(",")
        val stopIdIndex = headerColumns.indexOf("stop_id")
        val stopNameIndex = headerColumns.indexOf("stop_name")
        val stopLatIndex = headerColumns.indexOf("stop_lat")
        val stopLonIndex = headerColumns.indexOf("stop_lon")

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            try {
                val columns = line!!.split(",")
                if (columns.size >= 4) {
                    stops.add(
                        BusStopEntity(
                            stopId = columns[stopIdIndex],
                            stopName = columns[stopNameIndex],
                            stopLat = columns[stopLatIndex].toDoubleOrNull() ?: 0.0,
                            stopLon = columns[stopLonIndex].toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("BusRepository", "Error parsing stop: $line", e)
            }
        }
        reader.close()
        return stops
    }

    private fun loadTripsFromCsv(): List<BusTripEntity> {
        val trips = mutableListOf<BusTripEntity>()
        val inputStream = context.assets.open("GTFS/trips.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Parse header to get column indices
        val header = reader.readLine()
        val headerColumns = header.split(",")
        val tripIdIndex = headerColumns.indexOf("trip_id")
        val routeIdIndex = headerColumns.indexOf("route_id")
        val serviceIdIndex = headerColumns.indexOf("service_id")


        var line: String?
        while (reader.readLine().also { line = it } != null) {
            try {
                val columns = line!!.split(",")
                if (columns.size >= 3) {
                    trips.add(
                        BusTripEntity(
                            tripId = columns[tripIdIndex],
                            routeId = columns[routeIdIndex],
                            serviceId = columns[serviceIdIndex]
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("BusRepository", "Error parsing trip: $line", e)
            }
        }
        reader.close()
        return trips
    }

    private fun loadAgenciesFromCsv(): List<BusAgencyEntity> {
        val agencies = mutableListOf<BusAgencyEntity>()
        val inputStream = context.assets.open("GTFS/agency.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Skip header
        var header = reader.readLine()
        // Get column indexes
        val headerColumns = header.split(",")
        val idIndex = headerColumns.indexOf("agency_id")
        val nameIndex = headerColumns.indexOf("agency_name")
        val urlIndex = headerColumns.indexOf("agency_url")
        val timezoneIndex = headerColumns.indexOf("agency_timezone")

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            try {
                val columns = line!!.split(",")
                if (columns.size >= 4) {
                    agencies.add(
                        BusAgencyEntity(
                            agencyId = columns[idIndex],
                            agencyName = columns[nameIndex],
                            agencyUrl = columns[urlIndex],
                            agencyTimezone = columns[timezoneIndex]
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("BusRepository", "Error parsing agency: $line", e)
            }
        }
        reader.close()
        return agencies
    }

    private fun loadRoutesFromCsv(): List<BusRouteEntity> {
        val routes = mutableListOf<BusRouteEntity>()
        val inputStream = context.assets.open("GTFS/routes.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Parse header to get column indices
        val header = reader.readLine()
        val headerColumns = header.split(",")
        val routeIdIndex = headerColumns.indexOf("route_id")
        val agencyIdIndex = headerColumns.indexOf("agency_id")
        val routeShortNameIndex = headerColumns.indexOf("route_short_name")
        val routeLongNameIndex = headerColumns.indexOf("route_long_name")
        val routeTypeIndex = headerColumns.indexOf("route_type")

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            try {
                val columns = line!!.split(",")
                if (columns.size >= 5) {
                    routes.add(
                        BusRouteEntity(
                            routeId = columns[routeIdIndex],
                            agencyId = columns[agencyIdIndex],
                            routeShortName = columns[routeShortNameIndex],
                            routeLongName = columns[routeLongNameIndex],
                            routeType = columns[routeTypeIndex].toIntOrNull() ?: 3,
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("BusRepository", "Error parsing route: $line", e)
            }
        }
        reader.close()
        return routes
    }

    // Add to BusRepository
    suspend fun loadStopTimesFromCsv() {
        withContext(ioDispatcher) {
            try {
                val stopTimesFile = context.assets.open("GTFS/stop_times.txt")
                val reader = BufferedReader(InputStreamReader(stopTimesFile))

                // Read header
                val header = reader.readLine()
                val headerColumns = header.split(",")
                val tripIdIndex = headerColumns.indexOf("trip_id")
                val stopIdIndex = headerColumns.indexOf("stop_id")
                val arrivalTimeIndex = headerColumns.indexOf("arrival_time")
                val departureTimeIndex = headerColumns.indexOf("departure_time")
                val stopSequenceIndex = headerColumns.indexOf("stop_sequence")

                // Process in batches
                val batchSize = 500
                var batch = mutableListOf<StopTimeEntity>()
                var processedCount = 0

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    try {
                        val columns = line!!.split(",")
                        if (columns.size >= 5) {
                            val stopTime = StopTimeEntity(
                                tripId = columns[tripIdIndex],
                                stopId = columns[stopIdIndex],
                                arrivalTime = columns[arrivalTimeIndex],
                                departureTime = columns[departureTimeIndex],
                                stopSequence = columns[stopSequenceIndex].toIntOrNull() ?: 0
                            )

                            batch.add(stopTime)

                            if (batch.size >= batchSize) {
                                stopTimeDao.insertStopTimes(batch)
                                processedCount += batch.size
                                Log.d("BusRepository", "Processed $processedCount stop times")
                                batch = mutableListOf()

                                // Explicitly call garbage collection
                                System.gc()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BusRepository", "Error parsing stop time: $line", e)
                    }
                }

                // Insert any remaining records
                if (batch.isNotEmpty()) {
                    stopTimeDao.insertStopTimes(batch)
                    processedCount += batch.size
                    Log.d("BusRepository", "Processed $processedCount total stop times")
                }

                reader.close()
            } catch (e: Exception) {
                Log.e("BusRepository", "Error loading stop times: ${e.message}", e)
            }
        }
    }


    suspend fun searchBusStops(query: String): List<BusStop> {
        return withContext(ioDispatcher) {
            if (query.isBlank()) {
                return@withContext emptyList<BusStop>()
            }

            val stops = busStopDao.searchStops(query).first()
            return@withContext stops.map { it.toBusStop() }
        }
    }

    suspend fun findDirectRoutesBetweenStops(sourceStop: String, destinationStop: String): List<BusRouteWithStops> {
        return withContext(ioDispatcher) {
            Log.d("BusRepository", "Finding direct routes from $sourceStop to $destinationStop")

            val matchingRoutes = mutableListOf<BusRouteWithStops>()

            try {
                // Use the DAO query to find routes between stops
                val routeEntities = stopTimeDao.findRoutesBetweenStops(sourceStop, destinationStop).first()

                for (routeEntity in routeEntities) {
                    // Get trips for this route
                    val trips = busTripDao.getTripsByRouteId(routeEntity.routeId).first()

                    if (trips.isNotEmpty()) {
                        // Use the first trip to get ordered stops
                        val firstTrip = trips.first()
                        val stopEntities = stopTimeDao.getStopsByTripIdOrdered(firstTrip.tripId).first()

                        // Find source and destination stops
                        val sourceStopEntity = stopEntities.find {
                            it.stopName.contains(sourceStop, ignoreCase = true)
                        }

                        val destStopEntity = stopEntities.find {
                            it.stopName.contains(destinationStop, ignoreCase = true)
                        }

                        if (sourceStopEntity != null && destStopEntity != null) {
                            // Get index of source and destination in the sequence
                            val sourceIndex = stopEntities.indexOf(sourceStopEntity)
                            val destIndex = stopEntities.indexOf(destStopEntity)

                            // Ensure source comes before destination
                            if (sourceIndex >= 0 && destIndex >= 0 && sourceIndex < destIndex) {
                                // Create stops list in the correct order
                                val orderedStops = stopEntities.subList(sourceIndex, destIndex + 1)

                                matchingRoutes.add(
                                    BusRouteWithStops(
                                        route = routeEntity.toBusRoute(),
                                        stops = orderedStops.map { it.toBusStop() },
                                        startStop = sourceStopEntity.toBusStop(),
                                        endStop = destStopEntity.toBusStop()
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BusRepository", "Error finding direct routes: ${e.message}", e)
            }

            Log.d("BusRepository", "Found ${matchingRoutes.size} direct routes")
            return@withContext matchingRoutes
        }
    }

    suspend fun generateCombinedData() {
        withContext(ioDispatcher) {
            try {
                // Get all buses (trips)
                val trips = busTripDao.getAllTrips().first()

                // Get all routes
                val routes = busRouteDao.getAllRoutes().first()

                // Get all stops
                val stops = busStopDao.getAllStops().first()

                // Get all stop times
                val stopTimes = stopTimeDao.getAllStopTimes().first()

                // Create combined data
                val combinedData = mutableListOf<CombinedBusDataEntity>()

                for (trip in trips) {
                    val route = routes.find { it.routeId == trip.routeId } ?: continue

                    // Get stop times for this trip
                    val tripStopTimes = stopTimes.filter { it.tripId == trip.tripId }
                        .sortedBy { it.stopSequence }

                    for (stopTime in tripStopTimes) {
                        val stop = stops.find { it.stopId == stopTime.stopId } ?: continue

                        combinedData.add(
                            CombinedBusDataEntity(
                                busId = trip.tripId,
                                routeId = route.routeId,
                                routeName = route.routeLongName,
                                stopId = stop.stopId,
                                stopName = stop.stopName,
                                stopSequence = stopTime.stopSequence
                            )
                        )
                    }
                }

                // Insert combined data
                busRouteTripDao.insertCombinedData(combinedData)
                Log.d("BusRepository", "Generated ${combinedData.size} combined data records")
            } catch (e: Exception) {
                Log.e("BusRepository", "Error generating combined data: ${e.message}", e)
            }
        }
    }

    // Now implement the route finding methods using the HashMap approach
    suspend fun findBusesWithStops(sourceStop: String, destinationStop: String): List<BusRouteWithStops> {
        return withContext(ioDispatcher) {
            try {
                // Generate a HashMap structure similar to the example
                val busesMap = generateBusesHashMap()

                // Find matching buses
                val matchingBuses = mutableListOf<BusRouteWithStops>()

                for ((busId, routeStopsMap) in busesMap) {
                    for ((routeId, stops) in routeStopsMap) {
                        if (stops.contains(sourceStop) && stops.contains(destinationStop)) {
                            // Make sure source comes before destination in the sequence
                            val sourceIndex = stops.indexOf(sourceStop)
                            val destIndex = stops.indexOf(destinationStop)

                            if (sourceIndex >= 0 && destIndex >= 0 && sourceIndex < destIndex) {
                                // Get route details
                                val route = busRouteDao.getRouteById(routeId)

                                if (route != null) {
                                    // Get bus stops between source and destination
                                    val routeStops = stops.subList(sourceIndex, destIndex + 1)

                                    // Get BusStop objects for all stops in the route
                                    val busStops = routeStops.mapNotNull { stopName ->
                                        findStopByName(stopName)
                                    }

                                    if (busStops.size >= 2) {
                                        matchingBuses.add(
                                            BusRouteWithStops(
                                                route = route.toBusRoute(),
                                                stops = busStops,
                                                startStop = busStops.first(),
                                                endStop = busStops.last()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Log.d("BusRepository", "Found ${matchingBuses.size} direct routes")
                return@withContext matchingBuses
            } catch (e: Exception) {
                Log.e("BusRepository", "Error finding direct routes: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    private suspend fun generateBusesHashMap(): HashMap<String, HashMap<String, ArrayList<String>>> {
        val busesMap = HashMap<String, HashMap<String, ArrayList<String>>>()

        try {
            // Get all combined data
            val combinedData = busRouteTripDao.getAllCombinedData().first()

            // Group by busId and routeId
            for (data in combinedData) {
                val busId = data.busId
                val routeId = data.routeId
                val stopName = data.stopName

                // Get inner HashMap for this bus
                val routeStopsMap = busesMap.getOrPut(busId) { HashMap() }

                // Get list of stops for this route
                val stops = routeStopsMap.getOrPut(routeId) { ArrayList() }

                // Add stop if not already present
                if (!stops.contains(stopName)) {
                    stops.add(stopName)
                }
            }

            // Sort each stops list by sequence number
            for ((busId, routeStopsMap) in busesMap) {
                for ((routeId, stops) in routeStopsMap) {
                    val sortedStops = ArrayList<String>()

                    // Get combined data for this bus and route
                    val routeData = combinedData.filter { it.busId == busId && it.routeId == routeId }
                        .sortedBy { it.stopSequence }

                    // Add stops in sequence order
                    for (data in routeData) {
                        if (!sortedStops.contains(data.stopName)) {
                            sortedStops.add(data.stopName)
                        }
                    }

                    // Replace unsorted stops with sorted ones
                    routeStopsMap[routeId] = sortedStops
                }
            }
        } catch (e: Exception) {
            Log.e("BusRepository", "Error generating buses HashMap: ${e.message}", e)
        }

        return busesMap
    }

    // Graph-based route finding similar to the example
    suspend fun findRoutesByGraph(sourceStop: String, destinationStop: String): List<BusRouteWithStops> {
        return withContext(ioDispatcher) {
            try {
                // Generate HashMap for graph building
                val busesMap = generateBusesHashMap()

                // Build graph similar to example
                val graph = buildGraph(busesMap)

                // Use Dijkstra to find path
                val path = dijkstra(graph, sourceStop, destinationStop)

                if (path != null && path.isNotEmpty()) {
                    val routes = mutableListOf<BusRouteWithStops>()

                    // Group by bus/route
                    var currentBusId: String? = null
                    var currentRouteId: String? = null
                    var currentStops = mutableListOf<String>()

                    for (busInfo in path) {
                        // Example implementation returns bus ID and route name as a list
                        val busId = busInfo[0]
                        val routeId = busInfo[1]

                        if (currentBusId == null) {
                            currentBusId = busId
                            currentRouteId = routeId
                            currentStops.add(sourceStop)
                        } else if (busId != currentBusId || routeId != currentRouteId) {
                            // We've switched buses, add the completed route segment
                            if (currentStops.size >= 2 && currentRouteId != null) {
                                val route = busRouteDao.getRouteById(currentRouteId)

                                if (route != null) {
                                    val busStops = currentStops.mapNotNull { findStopByName(it) }

                                    if (busStops.size >= 2) {
                                        routes.add(
                                            BusRouteWithStops(
                                                route = route.toBusRoute(),
                                                stops = busStops,
                                                startStop = busStops.first(),
                                                endStop = busStops.last()
                                            )
                                        )
                                    }
                                }
                            }

                            // Start a new route segment
                            currentBusId = busId
                            currentRouteId = routeId
                            currentStops = mutableListOf(currentStops.last())
                        }

                        // Add next stop
                        // In the original example, the path doesn't include stop names
                        // So we need to look up the next stop based on bus and route
                        val nextStop = getNextStop(busId, routeId, currentStops.last())
                        if (nextStop != null && !currentStops.contains(nextStop)) {
                            currentStops.add(nextStop)
                        }
                    }

                    // Add the final destination
                    if (!currentStops.contains(destinationStop)) {
                        currentStops.add(destinationStop)
                    }

                    // Add the last route segment
                    if (currentStops.size >= 2 && currentRouteId != null) {
                        val route = busRouteDao.getRouteById(currentRouteId)

                        if (route != null) {
                            val busStops = currentStops.mapNotNull { findStopByName(it) }

                            if (busStops.size >= 2) {
                                routes.add(
                                    BusRouteWithStops(
                                        route = route.toBusRoute(),
                                        stops = busStops,
                                        startStop = busStops.first(),
                                        endStop = busStops.last()
                                    )
                                )
                            }
                        }
                    }

                    Log.d("BusRepository", "Found ${routes.size} routes using graph algorithm")
                    return@withContext routes
                } else {
                    Log.d("BusRepository", "No path found using graph algorithm")
                    return@withContext emptyList()
                }
            } catch (e: Exception) {
                Log.e("BusRepository", "Error finding routes by graph: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    private fun buildGraph(buses: HashMap<String, HashMap<String, ArrayList<String>>>): Map<String, Map<String, List<String>>> {
        val graph = mutableMapOf<String, MutableMap<String, List<String>>>()

        for ((busId, routeStopsMap) in buses) {
            for ((routeId, stops) in routeStopsMap) {
                for (i in 0 until stops.size - 1) {
                    val start = stops[i]
                    val end = stops[i + 1]
                    graph.getOrPut(start) { mutableMapOf() }[end] = listOf(busId, routeId)
                }
            }
        }

        return graph
    }

    private fun dijkstra(
        graph: Map<String, Map<String, List<String>>>,
        source: String,
        destination: String
    ): List<List<String>>? {
        val distances = mutableMapOf<String, Int>()
        val prev = mutableMapOf<String, Pair<String, List<String>>>()
        val queue = PriorityQueue<Pair<String, Int>> { a, b -> a.second.compareTo(b.second) }

        // Initialize
        distances[source] = 0
        queue.add(source to 0)

        while (queue.isNotEmpty()) {
            val (current, dist) = queue.poll()

            // Reached destination
            if (current == destination) {
                break
            }

            // Skip if we've found a better path
            if (dist > (distances[current] ?: Int.MAX_VALUE)) continue

            // Process neighbors
            for ((neighbor, busInfo) in graph[current] ?: emptyMap()) {
                val newDist = dist + 1

                if (newDist < (distances[neighbor] ?: Int.MAX_VALUE)) {
                    distances[neighbor] = newDist
                    prev[neighbor] = current to busInfo
                    queue.add(neighbor to newDist)
                }
            }
        }

        // Reconstruct path
        if (destination !in prev && source != destination) {
            return null  // No path found
        }

        val path = mutableListOf<List<String>>()
        var current = destination

        while (current != source) {
            val (prevStop, busInfo) = prev[current] ?: break
            path.add(0, busInfo)
            current = prevStop
        }

        return path
    }

    // Helper method to get the next stop
    private suspend fun getNextStop(busId: String, routeId: String, currentStop: String): String? {
        val combinedData = busRouteTripDao.getAllCombinedData().first()

        // Filter by bus and route
        val routeData = combinedData.filter { it.busId == busId && it.routeId == routeId }
            .sortedBy { it.stopSequence }

        // Find current stop index
        val currentIndex = routeData.indexOfFirst { it.stopName == currentStop }

        // Return next stop if available
        return if (currentIndex >= 0 && currentIndex < routeData.size - 1) {
            routeData[currentIndex + 1].stopName
        } else {
            null
        }
    }

    // Helper function to get route info for a bus
    private suspend fun getRouteInfoForBus(busId: String): Pair<String, String>? {
        try {
            val routes = busRouteDao.getAllRoutes().first()
            val route = routes.find { it.routeShortName == busId }

            if (route != null) {
                val trips = busTripDao.getTripsByRouteId(route.routeId).first()
                return Pair(route.routeId, route.routeLongName)
            }
        } catch (e: Exception) {
            Log.e("BusRepository", "Error getting route info for bus $busId: ${e.message}")
        }

        return null
    }

    // Helper function to find a stop by name
    private suspend fun findStopByName(stopName: String): BusStop? {
        try {
            val stops = busStopDao.searchStops(stopName).first()
            return if (stops.isNotEmpty()) stops.first().toBusStop() else null
        } catch (e: Exception) {
            Log.e("BusRepository", "Error finding stop by name $stopName: ${e.message}")
        }

        return null
    }

    suspend fun findRoutesBetweenStops(sourceStop: String, destStop: String): List<BusRouteWithStops> {
        return withContext(ioDispatcher) {
            // Get routes that connect these stops
            val routes = busStopSequenceDao.findRoutesBetweenStops(sourceStop, destStop).first()

            // For each route, get the complete list of stops
            val routesWithStops = routes.mapNotNull { route ->
                // Get first trip for this route
                val trips = busTripDao.getTripsByRouteId(route.routeId).first()
                if (trips.isNotEmpty()) {
                    val firstTrip = trips.first()

                    // Get all stops for this trip
                    val stops = busStopSequenceDao.getStopsByTripId(firstTrip.tripId).first()

                    // Find source and destination stops in the list
                    val sourceStopEntity =
                        stops.find { it.stopName.contains(sourceStop, ignoreCase = true) }
                    val destStopEntity =
                        stops.find { it.stopName.contains(destStop, ignoreCase = true) }

                    if (sourceStopEntity != null && destStopEntity != null) {
                        BusRouteWithStops(
                            route = route.toBusRoute(),
                            stops = stops.map { it.toBusStop() },
                            startStop = sourceStopEntity.toBusStop(),
                            endStop = destStopEntity.toBusStop(),
                        )
                    } else null
                } else null
            }

            return@withContext routesWithStops
        }
    }

    suspend fun getBusStop(stopId: String): BusStop? {
        return withContext(ioDispatcher) {
            val stopEntity = busStopDao.getStopById(stopId)
            stopEntity?.toBusStop()
        }
    }

    // Model conversion extensions
    private fun BusAgencyEntity.toBusAgency(): BusAgency {
        return BusAgency(
            agencyId = agencyId,
            agencyName = agencyName,
            agencyUrl = agencyUrl,
            agencyTimezone = agencyTimezone
        )
    }

    private fun BusRouteEntity.toBusRoute(): BusRoute {
        return BusRoute(
            routeId = routeId,
            agencyId = agencyId,
            routeShortName = routeShortName,
            routeLongName = routeLongName,
            routeType = routeType,
        )
    }

    private fun BusStopEntity.toBusStop(): BusStop {
        return BusStop(
            stopId = stopId,
            stopName = stopName,
            stopLat = stopLat,
            stopLon = stopLon
        )
    }

    private fun BusTripEntity.toBusTrip(): BusTrip {
        return BusTrip(
            tripId = tripId,
            routeId = routeId,
            serviceId = serviceId,
        )
    }
}