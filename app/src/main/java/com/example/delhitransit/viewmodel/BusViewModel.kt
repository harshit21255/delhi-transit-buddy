// BusViewModel.kt (new file)
package com.example.delhitransit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delhitransit.data.model.BusJourney
import com.example.delhitransit.data.model.BusRouteWithStops
import com.example.delhitransit.data.model.BusStop
import com.example.delhitransit.data.repository.BusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusViewModel @Inject constructor(
    private val repository: BusRepository
) : ViewModel() {

    private val TAG = "BusViewModel"

    private val _stops = MutableStateFlow<List<BusStop>>(emptyList())
    val stops: StateFlow<List<BusStop>> = _stops.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BusStop>>(emptyList())
    val searchResults: StateFlow<List<BusStop>> = _searchResults.asStateFlow()

    private val _routes = MutableStateFlow<List<BusRouteWithStops>>(emptyList())
    val routes: StateFlow<List<BusRouteWithStops>> = _routes.asStateFlow()

    private val _selectedJourney = MutableStateFlow<BusJourney?>(null)
    val selectedJourney: StateFlow<BusJourney?> = _selectedJourney.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                repository.initializeDatabaseFromGtfs()
            } catch (e: Exception) {
                _error.value = "Failed to initialize database: ${e.message}"
            }
        }
    }

    fun searchStops(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                } else {
                    _searchResults.value = repository.searchBusStops(query)
                }
            } catch (e: Exception) {
                _error.value = "Failed to search stops: ${e.message}"
                _searchResults.value = emptyList()
            }
        }
    }

    fun findRoutes(sourceStop: String, destinationStop: String) {
        viewModelScope.launch {
            try {
                // First try to find direct routes
                Log.d(TAG, "Finding direct routes from $sourceStop to $destinationStop")
                var routeResults = repository.findDirectRoutesBetweenStops(sourceStop, destinationStop)

                // If no direct routes, try graph-based approach
                if (routeResults.isEmpty()) {
                    Log.d(TAG, "No direct routes found, trying graph-based approach")
                    routeResults = repository.findRoutesByGraph(sourceStop, destinationStop)
                }

                _routes.value = routeResults

                // Log route information for debugging
                Log.i(TAG, "Found ${routeResults.size} routes from $sourceStop to $destinationStop")
                routeResults.forEach { route ->
                    Log.i(TAG, "Route: ${route.route.routeShortName} - ${route.route.routeLongName}")
                    Log.i(TAG, "Total stops: ${route.stops.size}")
                }

                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error finding routes", e)
                _error.value = "Failed to find routes: ${e.message}"
                _routes.value = emptyList()
            }
        }
    }

    fun selectJourney(sourceStop: BusStop, destStop: BusStop, route: BusRouteWithStops) {
        val journey = BusJourney(
            source = sourceStop,
            destination = destStop,
            route = route,
            totalStops = calculateTotalStops(route, sourceStop, destStop)
        )
        _selectedJourney.value = journey
    }

    private fun calculateTotalStops(route: BusRouteWithStops, source: BusStop, dest: BusStop): Int {
        val sourceIndex = route.stops.indexOfFirst { it.stopId == source.stopId }
        val destIndex = route.stops.indexOfFirst { it.stopId == dest.stopId }

        if (sourceIndex >= 0 && destIndex >= 0) {
            return Math.abs(destIndex - sourceIndex)
        }
        return route.stops.size - 1 // Fallback
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _routes.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedJourney() {
        _selectedJourney.value = null
    }
}