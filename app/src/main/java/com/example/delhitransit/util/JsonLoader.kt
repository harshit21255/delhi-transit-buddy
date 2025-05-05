package com.example.delhitransit.util

import android.content.Context
import android.util.Log
import com.example.delhitransit.data.model.Station
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

object JsonLoader {
    private val gson = GsonBuilder().setLenient().create()

    fun loadAllStations(context: Context): List<Station> {
        val allStations = mutableListOf<Station>()

        try {
            val jsonContent = readJsonFromAssets(context, "DMRC_STATIONS.json")

            val linesMap = gson.fromJson(jsonContent, object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type) as Map<String, List<Map<String, Any>>>

            linesMap.forEach { (lineName, stationsList) ->
                val lineStations = stationsList.mapNotNull { stationData ->
                    try {
                        val name = stationData["name"] as? String ?: return@mapNotNull null
                        val stationId = (stationData["stationId"] as? Double)?.toInt() ?: 0
                        val lat = stationData["lat"] as? Double ?: 0.0
                        val long = stationData["long"] as? Double ?: 0.0

                        Station(
                            name = name.trim(),
                            line = lineName.trim(),
                            stationId = stationId,
                            latitude = lat,
                            longitude = long
                        )
                    } catch (e: Exception) {
                        Log.e("JsonLoader", "Error parsing station in $lineName line: ${e.message}")
                        null
                    }
                }

                allStations.addAll(lineStations)
                Log.d("JsonLoader", "Successfully loaded ${lineStations.size} stations from $lineName line")
            }

        } catch (e: Exception) {
            Log.e("JsonLoader", "Error loading DMRC_STATIONS.json: ${e.message}")
            e.printStackTrace()
        }

        Log.d("JsonLoader", "Total stations loaded: ${allStations.size}")
        return allStations
    }

    // Load stations for a specific line
    fun loadStationsByLine(context: Context, lineName: String): List<Station> {
        val normalizedLineName = lineName.lowercase().trim()

        try {
            val jsonContent = readJsonFromAssets(context, "DMRC_STATIONS.json")
            val linesMap = gson.fromJson(jsonContent, object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type) as Map<String, List<Map<String, Any>>>

            val stationsList = linesMap[normalizedLineName] ?: return emptyList()

            return stationsList.mapNotNull { stationData ->
                try {
                    val name = stationData["name"] as? String ?: return@mapNotNull null
                    val stationId = (stationData["stationId"] as? Double)?.toInt() ?: 0
                    val lat = stationData["lat"] as? Double ?: 0.0
                    val long = stationData["long"] as? Double ?: 0.0

                    Station(
                        name = name.trim(),
                        line = normalizedLineName,
                        stationId = stationId,
                        latitude = lat,
                        longitude = long
                    )
                } catch (e: Exception) {
                    Log.e("JsonLoader", "Error parsing station in $normalizedLineName line: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("JsonLoader", "Error loading stations for line $normalizedLineName: ${e.message}")
            return emptyList()
        }
    }

    // Find stations by name (case insensitive partial match)
    fun findStationsByName(stations: List<Station>, name: String): List<Station> {
        val normalizedName = name.lowercase().trim()
        return stations.filter {
            it.name.lowercase().contains(normalizedName)
        }
    }

    // Helper method to read JSON file from assets
    private fun readJsonFromAssets(context: Context, filePath: String): String {
        val inputStream = context.assets.open(filePath)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()
        return stringBuilder.toString()
    }
}