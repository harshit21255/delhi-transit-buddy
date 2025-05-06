package com.example.delhitransit.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.delhitransit.data.model.Route
import com.example.delhitransit.data.model.Station
import com.example.delhitransit.viewmodel.MetroJourneyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyTrackingScreen(
    navController: NavController,
    viewModel: MetroJourneyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val journey by viewModel.journey.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Check location permission
    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission.value = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission.value) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(journey) {
        Log.d("JourneyTracking", "Journey received: ${journey?.source?.name} to ${journey?.destination?.name}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journey Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        // Journey header with source and destination
            JourneyHeader(journey = journey)

            Spacer(modifier = Modifier.height(16.dp))

            // Tracking toggle button
            TrackingToggle(
                isTracking = isTracking,
                hasLocationPermission = hasLocationPermission.value,
                onToggleTracking = { viewModel.toggleTracking() },
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current station card
            CurrentStationCard(
                station = currentStation,
                onPreviousStation = { viewModel.moveToPreviousStation() },
                onNextStation = { viewModel.moveToNextStation() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Journey progress
            JourneyProgress(
                journey = journey,
                currentStation = currentStation
            )
        }
    }
}

@Composable
fun JourneyHeader(journey: Route?) {
    journey?.let {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Your Journey",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = journey.source.name,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "To",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "To",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = journey.destination.name,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoBox(
                        label = "Stations",
                        value = "${journey.totalStations + 1}"
                    )
                    InfoBox(
                        label = "Interchanges",
                        value = "${journey.interchangeCount}"
                    )
                }
            }
        }
    } ?: run {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "No journey loaded",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun InfoBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TrackingToggle(
    isTracking: Boolean,
    hasLocationPermission: Boolean,
    onToggleTracking: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTracking)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isTracking) "Tracking Active" else "Tracking Inactive",
                fontWeight = FontWeight.Bold,
                color = if (isTracking)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isTracking)
                    "You'll be notified when approaching stations"
                else
                    "Enable tracking to get station notifications",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (isTracking)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (hasLocationPermission) {
                Switch(
                    checked = isTracking,
                    onCheckedChange = { onToggleTracking() },
                    thumbContent = {
                        Icon(
                            imageVector = if (isTracking) Icons.Default.LocationOn else Icons.Default.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
            } else {
                Button(
                    onClick = { onRequestPermission() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Grant Location Permission")
                }
            }
        }
    }
}

@Composable
fun CurrentStationCard(
    station: Station?,
    onPreviousStation: () -> Unit,
    onNextStation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Station",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            station?.let {
                Text(
                    text = station.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(getLineColor(station.line))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = getLineName(station.line),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onPreviousStation() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Previous Station"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = { onNextStation() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next Station"
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "No current station",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun JourneyProgress(journey: Route?, currentStation: Station?) {
    journey?.let { route ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Journey Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                route.path.forEachIndexed { index, station ->
                    val isCurrentStation = currentStation?.name == station.name &&
                            currentStation.line == station.line
                    val isPastStation = if (currentStation != null) {
                        val currentIndex = route.path.indexOfFirst {
                            it.name == currentStation.name && it.line == currentStation.line
                        }
                        index < currentIndex
                    } else false

                    StationProgressItem(
                        station = station,
                        isCurrentStation = isCurrentStation,
                        isPastStation = isPastStation,
                        isInterchange = index < route.path.size - 1 &&
                                station.line != route.path[index + 1].line,
                        isLastStation = index == route.path.size - 1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val currentIndex = route.path.indexOfFirst {
                    it.name == currentStation?.name && it.line == currentStation.line
                }.coerceAtLeast(0)

                val progress = (currentIndex + 1).toFloat() / route.path.size

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun StationProgressItem(
    station: Station,
    isCurrentStation: Boolean,
    isPastStation: Boolean,
    isInterchange: Boolean,
    isLastStation: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Station indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = when {
                        isCurrentStation -> MaterialTheme.colorScheme.primary
                        isPastStation -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPastStation) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Connecting line (not for last station)
        if (!isLastStation) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(
                        color = if (isPastStation)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }

        // Station details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = station.name,
                fontWeight = if (isCurrentStation) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentStation)
                    MaterialTheme.colorScheme.primary
                else if (isPastStation)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = getLineName(station.line),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Interchange indicator
        if (isInterchange) {
            Icon(
                imageVector = Icons.Default.ChangeCircle,
                contentDescription = "Interchange",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Add connecting line between stations
    if (!isLastStation) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(2.dp)
                .height(16.dp)
                .background(
                    color = if (isPastStation)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
        )
    }
}

