// BusJourneyTrackingScreen.kt (new file)
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.delhitransit.data.model.BusJourney
import com.example.delhitransit.data.model.BusStop
import com.example.delhitransit.ui.theme.DelhiOrange
import com.example.delhitransit.viewmodel.BusJourneyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusJourneyTrackingScreen(
    navController: NavController,
    viewModel: BusJourneyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val journey by viewModel.journey.collectAsState()
    val currentStop by viewModel.currentStop.collectAsState()
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
        Log.d("BusJourneyTracking", "Journey received: ${journey?.source?.stopName} to ${journey?.destination?.stopName}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bus Journey Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DelhiOrange,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
            BusJourneyHeader(journey = journey)

            Spacer(modifier = Modifier.height(16.dp))

            // Tracking toggle button
            BusTrackingToggle(
                isTracking = isTracking,
                hasLocationPermission = hasLocationPermission.value,
                onToggleTracking = { viewModel.toggleTracking() },
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current stop card
            BusCurrentStopCard(
                stop = currentStop,
                onPreviousStop = { viewModel.moveToPreviousStop() },
                onNextStop = { viewModel.moveToNextStop() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Journey progress
            BusJourneyProgress(
                journey = journey,
                currentStop = currentStop
            )
        }
    }
}

@Composable
fun BusJourneyHeader(journey: BusJourney?) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Blue)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = journey.route.route.routeShortName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Your Bus Journey",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "From",
                            fontSize = 12.sp,
                            color = DelhiOrange
                        )
                        Text(
                            text = journey.source.stopName,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "To",
                        tint = DelhiOrange
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "To",
                            fontSize = 12.sp,
                            color = DelhiOrange,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = journey.destination.stopName,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BusJourneyInfoBox(
                        label = "Stops",
                        value = "${journey.totalStops}"
                    )

                    BusJourneyInfoBox(
                        label = "Route",
                        value = journey.route.route.routeShortName
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
fun BusJourneyInfoBox(label: String, value: String) {
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
fun BusTrackingToggle(
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
                    "You'll be notified when approaching stops"
                else
                    "Enable tracking to get stop notifications",
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
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DelhiOrange,
                        checkedTrackColor = DelhiOrange.copy(alpha = 0.5f)
                    )
                )
            } else {
                Button(
                    onClick = { onRequestPermission() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DelhiOrange
                    )
                ) {
                    Text("Grant Location Permission")
                }
            }
        }
    }
}

@Composable
fun BusCurrentStopCard(
    stop: BusStop?,
    onPreviousStop: () -> Unit,
    onNextStop: () -> Unit
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
                text = "Current Stop",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            stop?.let {
                Text(
                    text = stop.stopName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onPreviousStop() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DelhiOrange
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Previous Stop"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = { onNextStop() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DelhiOrange
                        )
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next Stop"
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "No current stop",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun BusJourneyProgress(journey: BusJourney?, currentStop: BusStop?) {
    journey?.let { busJourney ->
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

                // Find indices for source and destination
                val sourceIndex = busJourney.route.stops.indexOfFirst {
                    it.stopId == busJourney.source.stopId
                }
                val destIndex = busJourney.route.stops.indexOfFirst {
                    it.stopId == busJourney.destination.stopId
                }

                if (sourceIndex >= 0 && destIndex >= 0) {
                    // Determine direction of travel
                    val startIndex = if (sourceIndex <= destIndex) sourceIndex else destIndex
                    val endIndex = if (sourceIndex <= destIndex) destIndex else sourceIndex

                    // Display stops in the journey
                    for (i in startIndex..endIndex) {
                        val stop = busJourney.route.stops[i]
                        val isCurrentStop = currentStop?.stopId == stop.stopId
                        val isPastStop = if (currentStop != null) {
                            val currentIndex = busJourney.route.stops.indexOfFirst {
                                it.stopId == currentStop.stopId
                            }
                            if (sourceIndex <= destIndex) {
                                i < currentIndex
                            } else {
                                i > currentIndex
                            }
                        } else false

                        BusStopProgressItem(
                            stop = stop,
                            isCurrentStop = isCurrentStop,
                            isPastStop = isPastStop,
                            isFirstStop = i == startIndex,
                            isLastStop = i == endIndex
                        )
                    }
                }


            }
        }
    }
}

@Composable
fun BusStopProgressItem(
    stop: BusStop,
    isCurrentStop: Boolean,
    isPastStop: Boolean,
    isFirstStop: Boolean,
    isLastStop: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Stop indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = when {
                        isCurrentStop -> DelhiOrange
                        isPastStop -> DelhiOrange.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPastStop) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Connecting line (not for last stop)
        if (!isLastStop) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(
                        color = if (isPastStop)
                            DelhiOrange.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }

        // Stop details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = stop.stopName,
                fontWeight = if (isCurrentStop || isFirstStop || isLastStop) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentStop)
                    DelhiOrange
                else if (isPastStop)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = when {
                    isFirstStop -> "Departure"
                    isLastStop -> "Destination"
                    else -> "Bus Stop"
                },
                fontSize = 12.sp,
                color = when {
                    isCurrentStop -> DelhiOrange
                    isFirstStop || isLastStop -> DelhiOrange.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }

    // Add connecting line between stops
    if (!isLastStop) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(2.dp)
                .height(16.dp)
                .background(
                    color = if (isPastStop)
                        DelhiOrange.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
        )
    }
}