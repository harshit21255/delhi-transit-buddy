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
import com.example.delhitransit.data.model.BusJourney
import com.example.delhitransit.data.model.BusStop
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
class BusJourneyViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "BusJourneyViewModel"

    private val TRACKING_INTERVAL = 30000L // 30 seconds
    private val PROXIMITY_THRESHOLD = 100.0 // 100 meters

    private val _journey = MutableStateFlow<BusJourney?>(null)
    val journey: StateFlow<BusJourney?> = _journey.asStateFlow()

    private val _currentStop = MutableStateFlow<BusStop?>(null)
    val currentStop: StateFlow<BusStop?> = _currentStop.asStateFlow()

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

    fun setJourney(journey: BusJourney) {
        _journey.value = journey
        // Set first stop as current
        if (journey.route.stops.isNotEmpty()) {
            val sourceStopIndex = journey.route.stops.indexOfFirst { it.stopId == journey.source.stopId }
            if (sourceStopIndex >= 0) {
                _currentStop.value = journey.route.stops[sourceStopIndex]
            } else {
                _currentStop.value = journey.route.stops.first()
            }
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

    fun moveToPreviousStop() {
        val journey = _journey.value ?: return
        val currentIndex = getCurrentStopIndex()

        if (currentIndex > 0) {
            _currentStop.value = journey.route.stops[currentIndex - 1]
        }
    }

    fun moveToNextStop() {
        val journey = _journey.value ?: return
        val currentIndex = getCurrentStopIndex()

        if (currentIndex < journey.route.stops.size - 1) {
            _currentStop.value = journey.route.stops[currentIndex + 1]
        }
    }

    private fun getCurrentStopIndex(): Int {
        val journey = _journey.value ?: return -1
        val current = _currentStop.value ?: return -1

        return journey.route.stops.indexOfFirst {
            it.stopId == current.stopId
        }
    }

    private fun startLocationTracking() {
        trackingJob?.cancel()

        trackingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    updateUserLocation()
                    checkProximityToNextStop()
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

    private fun checkProximityToNextStop() {
        val userLocation = _userLocation.value ?: return
        val journey = _journey.value ?: return
        val currentIndex = getCurrentStopIndex()

        if (currentIndex < 0 || currentIndex >= journey.route.stops.size - 1) return

        val nextStop = journey.route.stops[currentIndex + 1]

        val distance = calculateDistance(
            userLocation.latitude, userLocation.longitude,
            nextStop.stopLat, nextStop.stopLon
        )

        Log.d(TAG, "Distance to ${nextStop.stopName}: $distance meters")

        if (distance <= PROXIMITY_THRESHOLD) {
            // User is near the next stop
            sendStopProximityNotification(nextStop)
            // Auto update to next stop
            _currentStop.value = nextStop
        }
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

    private fun sendStopProximityNotification(stop: BusStop) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus)
            .setContentTitle("Approaching bus stop")
            .setContentText("You're approaching ${stop.stopName}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stop Proximity"
            val descriptionText = "Notifications for approaching bus stops"
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

    companion object {
        private const val CHANNEL_ID = "bus_stop_proximity_channel"
        private const val NOTIFICATION_ID = 2001
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}