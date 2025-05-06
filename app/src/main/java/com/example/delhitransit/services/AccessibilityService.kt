package com.example.delhitransit.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.delhitransit.data.model.BusJourney
import com.example.delhitransit.data.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * Service to handle Text-to-Speech functionality for accessibility
 */
class AccessibilityService(context: Context) {
    private val TAG = "AccessibilityService"

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    // Flow to track speaking state
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    Log.d(TAG, "TTS initialized successfully")
                    isTtsReady = true

                    // Set up listener for speaking status
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {
                            _isSpeaking.value = true
                            Log.d(TAG, "TTS started speaking: $utteranceId")
                        }

                        override fun onDone(utteranceId: String) {
                            _isSpeaking.value = false
                            Log.d(TAG, "TTS finished speaking: $utteranceId")
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String) {
                            _isSpeaking.value = false
                            Log.e(TAG, "TTS error for utterance: $utteranceId")
                        }
                    })
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
            }
        }
    }

    /**
     * Speak metro route directions aloud
     */
    fun speakMetroRoute(route: Route) {
        if (!isTtsReady || textToSpeech == null) {
            Log.e(TAG, "TTS not ready")
            return
        }

        val utteranceId = UUID.randomUUID().toString()

        // Count interchanges and construct route description
        val interchanges = getMetroInterchanges(route)
        val routeDescription = buildMetroRouteDescription(route, interchanges)

        Log.d(TAG, "Speaking route: $routeDescription")
        textToSpeech?.speak(routeDescription, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Speak bus route directions aloud
     */
    fun speakBusRoute(journey: BusJourney) {
        if (!isTtsReady || textToSpeech == null) {
            Log.e(TAG, "TTS not ready")
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        val routeDescription = buildBusRouteDescription(journey)

        Log.d(TAG, "Speaking bus route: $routeDescription")
        textToSpeech?.speak(routeDescription, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Stop speaking immediately
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    /**
     * Get list of interchange stations from route
     */
    private fun getMetroInterchanges(route: Route): List<Pair<Int, String>> {
        val interchanges = mutableListOf<Pair<Int, String>>()

        if (route.path.size < 2) return interchanges

        for (i in 0 until route.path.size - 1) {
            val currentStation = route.path[i]
            val nextStation = route.path[i + 1]

            if (currentStation.name == nextStation.name && currentStation.line != nextStation.line) {
                // This is an interchange - same station name but different line
                interchanges.add(Pair(i, nextStation.line))
            }
        }

        return interchanges
    }

    /**
     * Build a human-friendly description of the metro route
     */
    private fun buildMetroRouteDescription(route: Route, interchanges: List<Pair<Int, String>>): String {
        val sb = StringBuilder()

        sb.append("The best route from ${route.source.name} to ${route.destination.name} is as follows: ")

        if (interchanges.isEmpty()) {
            // Direct route with no interchanges
            sb.append("Take the ${getLineName(route.source.line)} for ${route.totalStations} stations and you'll reach your destination.")
        } else {
            // Route with interchanges
            var currentIndex = 0
            var currentLine = route.source.line

            for (interchange in interchanges) {
                val changeIndex = interchange.first
                val nextLine = interchange.second
                val stationCount = changeIndex - currentIndex

                sb.append("Take the ${getLineName(currentLine)} for $stationCount stations till ${route.path[changeIndex].name}")
                sb.append(" and switch to ${getLineName(nextLine)}. ")

                currentIndex = changeIndex + 1
                currentLine = nextLine
            }

            // Add the final segment
            val remainingStations = route.path.size - 1 - currentIndex
            if (remainingStations > 0) {
                sb.append("Continue on ${getLineName(currentLine)} for $remainingStations stations and you'll reach your destination.")
            } else {
                sb.append("You'll then reach your destination.")
            }
        }

        return sb.toString()
    }

    /**
     * Build a human-friendly description of the bus route
     */
    private fun buildBusRouteDescription(journey: BusJourney): String {
        val sb = StringBuilder()

        sb.append("The best route from ${journey.source.stopName} to ${journey.destination.stopName} is as follows: ")

        // Get bus number and route information
        val busNumber = journey.route.route.routeShortName
        val routeName = journey.route.route.routeLongName

        sb.append("Take bus number $busNumber ($routeName) for ${journey.totalStops} stops and you'll reach your destination.")

        return sb.toString()
    }

    /**
     * Get a human-friendly name for a metro line
     */
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

    /**
     * Clean up TTS resources
     */
    fun cleanup() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}