package com.example.delhitransit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delhitransit.data.model.MetroLine
import com.example.delhitransit.data.model.Route
import com.example.delhitransit.data.model.Station
import com.example.delhitransit.data.repository.MetroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetroViewModel @Inject constructor(
    private val repository: MetroRepository,
) : ViewModel() {

    private val TAG = "MetroViewModel"

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Station>>(emptyList())
    val searchResults: StateFlow<List<Station>> = _searchResults.asStateFlow()

    private val _lines = MutableStateFlow<List<MetroLine>>(emptyList())
    val lines: StateFlow<List<MetroLine>> = _lines.asStateFlow()

    private val _route = MutableStateFlow<Route?>(null)
    val route: StateFlow<Route?> = _route.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Collect stations from repository Flow
        viewModelScope.launch {
            repository.allStationsFlow.collect { stationsList ->
                _stations.value = stationsList
            }
        }

        // Collect lines from repository Flow
        viewModelScope.launch {
            repository.allLinesFlow.collect { linesList ->
                _lines.value = linesList
            }
        }

        // Initialize the database if needed
        viewModelScope.launch {
            try {
                repository.initializeDatabaseFromJson()
            } catch (e: Exception) {
                _error.value = "Failed to initialize database: ${e.message}"
            }
        }
    }

    fun findRoute(sourceStation: String, destinationStation: String) {
        viewModelScope.launch {
            try {
                val routeResult = repository.getRoute(sourceStation, destinationStation)
                _route.value = routeResult

                // Log route information for debugging
                Log.i(TAG, "Found route from $sourceStation to $destinationStation")
                Log.i(TAG, "Total stations: ${routeResult.totalStations}")
                Log.i(TAG, "Interchanges: ${routeResult.interchangeCount}")
                Log.i(TAG, "Path size: ${routeResult.path.size}")

                // Log all stations in the path
                routeResult.path.forEachIndexed { index, station ->
                    Log.i(TAG, "Station $index: ${station.name} (${station.line})")
                }

                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error finding route", e)
                _error.value = "Failed to find route: ${e.message}"
                _route.value = null
            }
        }
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                } else {
                    // Use the searchStation function from repository
                    _searchResults.value = repository.searchStation(query)
                }
            } catch (e: Exception) {
                _error.value = "Failed to search stations: ${e.message}"
                _searchResults.value = emptyList()
            }
        }
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _route.value = null
    }

    fun clearError() {
        _error.value = null
    }
}