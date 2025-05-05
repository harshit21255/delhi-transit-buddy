package com.example.delhitransit.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delhitransit.R
import com.example.delhitransit.data.model.Route
import com.example.delhitransit.data.model.Station
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class JourneyViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "JourneyViewModel"


    private val TRACKING_INTERVAL = 30000L // 30 seconds
    private val PROXIMITY_THRESHOLD = 100.0 // 100 meters

    private val _journey = MutableStateFlow<Route?>(null)
    val journey: StateFlow<Route?> = _journey.asStateFlow()

    private val _currentStation = MutableStateFlow<Station?>(null)
    val currentStation: StateFlow<Station?> = _currentStation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    private var trackingJob: Job? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        createNotificationChannel()
    }

    fun setJourney(route: Route) {
        _journey.value = route
        // Set first station as current
        if (route.path.isNotEmpty()) {
            _currentStation.value = route.path.first()
        }
    }

    fun toggleTracking() {
        _isTracking.value = !_isTracking.value

        if (_isTracking.value) {
            startLocationTracking()
        } else {
            stopLocationTracking()
        }
    }

    fun moveToPreviousStation() {
        val route = _journey.value ?: return
        val currentIndex = getCurrentStationIndex()

        if (currentIndex > 0) {
            _currentStation.value = route.path[currentIndex - 1]
        }
    }

    fun moveToNextStation() {
        val route = _journey.value ?: return
        val currentIndex = getCurrentStationIndex()

        if (currentIndex < route.path.size - 1) {
            _currentStation.value = route.path[currentIndex + 1]
        }
    }

    private fun getCurrentStationIndex(): Int {
        val route = _journey.value ?: return -1
        val current = _currentStation.value ?: return -1

        return route.path.indexOfFirst {
            it.name == current.name && it.line == current.line
        }
    }

    private fun startLocationTracking() {
        trackingJob?.cancel()

        trackingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    updateUserLocation()
                    checkProximityToNextStation()
                    delay(TRACKING_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in tracking: ${e.message}")
                }
            }
        }
    }

    private fun stopLocationTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private suspend fun updateUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _userLocation.value = location
                    Log.d(TAG, "Updated user location: ${location.latitude}, ${location.longitude}")
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted: ${e.message}")
        }
    }

    private fun checkProximityToNextStation() {
        val userLocation = _userLocation.value ?: return
        val route = _journey.value ?: return
        val currentIndex = getCurrentStationIndex()

        if (currentIndex < 0 || currentIndex >= route.path.size - 1) return

        val nextStation = route.path[currentIndex + 1]
        val stationLocation = getStationLocation(nextStation)

        if (stationLocation != null) {
            val distance = calculateDistance(
                userLocation.latitude, userLocation.longitude,
                stationLocation.first, stationLocation.second
            )

            Log.d(TAG, "Distance to ${nextStation.name}: $distance meters")

            if (distance <= PROXIMITY_THRESHOLD) {
                // User is near the next station
                sendStationProximityNotification(nextStation)
                // Auto update to next station
                _currentStation.value = nextStation
            }
        }
    }

    private fun getStationLocation(station: Station): Pair<Double, Double> {
        return Pair(station.latitude, station.longitude)
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val lonDiffRad = Math.toRadians(lon2 - lon1)

        val a = sin((lat2Rad - lat1Rad) / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(lonDiffRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun sendStationProximityNotification(station: Station) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_metro)
            .setContentTitle("Approaching station")
            .setContentText("You're approaching ${station.name} (${getLineName(station.line)})")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Station Proximity"
            val descriptionText = "Notifications for approaching metro stations"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun Double.pow(n: Int): Double = Math.pow(this, n.toDouble())

    private fun getLineName(line: String): String {
        return when (line.lowercase()) {
            "yellow" -> "Yellow Line"
            "blue" -> "Blue Line"
            "red" -> "Red Line"
            "green" -> "Green Line"
            "violet" -> "Violet Line"
            "orange" -> "Orange Line"
            "magenta" -> "Magenta Line"
            "pink" -> "Pink Line"
            "aqua" -> "Aqua Line"
            "grey" -> "Grey Line"
            "rapid" -> "Rapid Metro"
            "greenbranch" -> "Green Line Branch"
            "bluebranch" -> "Blue Line Branch"
            "pinkbranch" -> "Pink Line Branch"
            else -> line
        }
    }

    companion object {
        private const val CHANNEL_ID = "station_proximity_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}