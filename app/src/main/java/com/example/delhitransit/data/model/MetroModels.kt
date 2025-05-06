package com.example.delhitransit.data.model

data class Station(
    val name: String,
    val line: String,
    val stationId: Int,
    val latitude: Double,
    val longitude: Double
)

data class MetroLine(
    val name: String,
    val stations: List<Station>
)

data class Route(
    val source: Station,
    val destination: Station,
    val path: List<Station>,
    val totalStations: Int,
    val interchangeCount: Int,
    val estimatedTime: Int = 0
)

data class GraphNode(
    val station: Station,
    var distance: Int = Int.MAX_VALUE,
    var previousNode: GraphNode? = null,
    var visited: Boolean = false
) {
    fun compareTo(other: GraphNode): Int {
        return distance.compareTo(other.distance)
    }
} 