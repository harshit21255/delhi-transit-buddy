package com.example.delhitransit.ui.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages theme preferences and automatic light detection for theme switching
 */
class ThemeManager(context: Context) {
    private val TAG = "ThemeManager"
    private val LIGHT_THRESHOLD = 500f

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Theme state flows
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isHighContrastMode = MutableStateFlow(false)
    val isHighContrastMode: StateFlow<Boolean> = _isHighContrastMode.asStateFlow()

    private val _isAutoMode = MutableStateFlow(true)
    val isAutoMode: StateFlow<Boolean> = _isAutoMode.asStateFlow()

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_LIGHT && _isAutoMode.value) {
                val lightLevel = event.values[0]
                Log.d(TAG, "Light sensor reading: $lightLevel lux")

                // Update dark mode based on light level
                val shouldBeDarkMode = lightLevel < LIGHT_THRESHOLD
                if (_isDarkMode.value != shouldBeDarkMode) {
                    Log.d(TAG, "Switching to ${if (shouldBeDarkMode) "dark" else "light"} mode")
                    _isDarkMode.value = shouldBeDarkMode
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not needed for this implementation
        }
    }

    init {
        // Register for light sensor updates if available
        if (lightSensor != null) {
            sensorManager.registerListener(
                lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Light sensor registered")
        } else {
            Log.w(TAG, "Light sensor not available on this device")
        }
    }

    fun toggleDarkMode() {
        _isAutoMode.value = false
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleHighContrastMode() {
        _isHighContrastMode.value = !_isHighContrastMode.value
    }

    fun toggleAutoMode() {
        _isAutoMode.value = !_isAutoMode.value
    }

    fun cleanup() {
        sensorManager.unregisterListener(lightSensorListener)
    }
}