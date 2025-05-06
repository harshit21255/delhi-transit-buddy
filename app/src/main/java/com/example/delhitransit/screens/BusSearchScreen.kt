// BusSearchScreen.kt (new file)
package com.example.delhitransit.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.delhitransit.data.model.BusJourney
import com.example.delhitransit.data.model.BusRouteWithStops
import com.example.delhitransit.data.model.BusStop
import com.example.delhitransit.ui.theme.DelhiOrange
import com.example.delhitransit.viewmodel.BusJourneyViewModel
import com.example.delhitransit.viewmodel.BusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScreen(
    navController: NavController,
    busViewModel: BusViewModel = hiltViewModel(),
    busJourneyViewModel: BusJourneyViewModel = hiltViewModel()
) {
    val searchResults by busViewModel.searchResults.collectAsState()
    val routes by busViewModel.routes.collectAsState()
    val selectedJourney by busViewModel.selectedJourney.collectAsState()
    val error by busViewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var sourceStop by remember { mutableStateOf("") }
    var destinationStop by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf<String?>(null) }

    // When a journey is selected, immediately set it in the BusJourneyViewModel
    LaunchedEffect(selectedJourney) {
        selectedJourney?.let {
            busJourneyViewModel.setJourney(it)
        }
    }

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            busViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DelhiOrange,
                                DelhiOrange.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delhi Bus",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Similar search fields as MetroScreen but themed for Bus
                    JourneySearchCard(
                        sourceStop = sourceStop,
                        destinationStop = destinationStop,
                        onSourceClick = { selectionMode = "source" },
                        onDestinationClick = { selectionMode = "destination" },
                        onFindRouteClick = {
                            if (sourceStop.isNotEmpty() && destinationStop.isNotEmpty()) {
                                busViewModel.findRoutes(sourceStop, destinationStop)
                            }
                        },
                        primaryColor = DelhiOrange
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selection mode UI (similar to MetroScreen)
            if (selectionMode != null) {
                SearchModeHeader(
                    selectionMode = selectionMode!!,
                    onCancelClick = { selectionMode = null },
                    primaryColor = DelhiOrange
                )

                // Search Stop
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        busViewModel.searchStops(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Search for bus stop") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DelhiOrange
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                busViewModel.searchStops("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DelhiOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = DelhiOrange,
                        cursorColor = DelhiOrange
                    ),
                    singleLine = true
                )

                // Show search results if available
                if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(searchResults) { stop ->
                            BusStopItem(
                                stop = stop,
                                onClick = {
                                    when (selectionMode) {
                                        "source" -> sourceStop = stop.stopName
                                        "destination" -> destinationStop = stop.stopName
                                    }
                                    selectionMode = null
                                    searchQuery = ""
                                    busViewModel.clearResults()
                                }
                            )
                        }
                    }
                } else if (searchQuery.isNotEmpty()) {
                    // No results found UI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = DelhiOrange.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No bus stops found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (routes.isNotEmpty() && selectedJourney == null) {
                // Show route options
                BusRoutesList(
                    routes = routes,
                    sourceStop = sourceStop,
                    destinationStop = destinationStop,
                    onRouteSelected = { route ->
                        // Find the exact BusStop objects
                        val source = route.stops.find { it.stopName.contains(sourceStop, ignoreCase = true) }
                        val destination = route.stops.find { it.stopName.contains(destinationStop, ignoreCase = true) }

                        if (source != null && destination != null) {
                            busViewModel.selectJourney(source, destination, route)
                        }
                    }
                )
            } else if (selectedJourney != null) {
                // Show selected journey details with begin button
                BusJourneyDetails(
                    journey = selectedJourney!!,
                    onBeginJourney = {
                        navController.navigate("bus_journey_tracking")
                    },
                    onBack = {
                        busViewModel.clearSelectedJourney()
                    }
                )
            } else {
                // Welcome screen when no routes or search is active
                BusWelcomeScreen()
            }
        }
    }
}

@Composable
fun JourneySearchCard(
    sourceStop: String,
    destinationStop: String,
    onSourceClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onFindRouteClick: () -> Unit,
    primaryColor: Color = DelhiOrange
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Source field
            OutlinedTextField(
                value = sourceStop,
                onValueChange = { /* Readonly */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSourceClick),
                readOnly = true,
                label = { Text("From") },
                placeholder = { Text("Select departure stop") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "From",
                        tint = primaryColor
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = primaryColor,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Direction icon
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap stops",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Destination field
            OutlinedTextField(
                value = destinationStop,
                onValueChange = { /* Readonly */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDestinationClick),
                readOnly = true,
                label = { Text("To") },
                placeholder = { Text("Select destination stop") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "To",
                        tint = primaryColor
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = primaryColor,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Find route button
            Button(
                onClick = onFindRouteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = sourceStop.isNotEmpty() && destinationStop.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Find Route",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SearchModeHeader(
    selectionMode: String,
    onCancelClick: () -> Unit,
    primaryColor: Color = DelhiOrange
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (selectionMode == "source") Icons.Default.Place else Icons.Default.LocationOn

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Select ${if (selectionMode == "source") "departure" else "destination"} stop",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onCancelClick
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun BusStopItem(
    stop: BusStop,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bus icon indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DelhiOrange)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stop.stopName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Bus Stop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Select",
                tint = DelhiOrange,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BusRoutesList(
    routes: List<BusRouteWithStops>,
    sourceStop: String,
    destinationStop: String,
    onRouteSelected: (BusRouteWithStops) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "AVAILABLE BUS ROUTES",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = DelhiOrange,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Route count
        Text(
            text = "Found ${routes.size} routes between $sourceStop and $destinationStop",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Routes list
        LazyColumn {
            items(routes) { route ->
                BusRouteItem(
                    route = route,
                    onClick = { onRouteSelected(route) }
                )
            }
        }
    }
}

@Composable
fun BusRouteItem(
    route: BusRouteWithStops,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Route number and name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Blue)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = route.route.routeShortName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.route.routeLongName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Journey info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Starting stop
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = route.startStop.stopName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Connecting arrow
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = DelhiOrange,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                )

                // Ending stop
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "TO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = route.endStop.stopName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BusRouteStat(
                    icon = Icons.Default.LocationOn,
                    label = "Total Stops",
                    value = "${route.stops.size}"
                )

                BusRouteStat(
                    icon = Icons.Default.DirectionsBus,
                    label = "Vehicle Type",
                    value = getBusType(route.route.routeType)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Select button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DelhiOrange
                )
            ) {
                Text(
                    text = "Select Route",
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BusRouteStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DelhiOrange,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BusJourneyDetails(
    journey: BusJourney,
    onBeginJourney: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            // Journey summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = DelhiOrange,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Your Bus Journey",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Bus ${journey.route.route.routeShortName} • ${journey.totalStops} stops",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Journey endpoints
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start stop
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "FROM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = journey.source.stopName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 8.dp)
                        )

                        // End stop
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "TO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = journey.destination.stopName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Route card with color
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Blue)
                                    .border(1.dp, Color.White, CircleShape),
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = journey.route.route.routeLongName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DelhiOrange)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = DelhiOrange
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Back",
                                color = DelhiOrange
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onBeginJourney,
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DelhiOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Begin Journey",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Journey stops header
            Text(
                text = "JOURNEY STOPS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = DelhiOrange,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
            )
        }

        // List all stops in the journey
        val sourceIndex = journey.route.stops.indexOfFirst { it.stopId == journey.source.stopId }
        val destinationIndex = journey.route.stops.indexOfFirst { it.stopId == journey.destination.stopId }

        if (sourceIndex >= 0 && destinationIndex >= 0) {
            val startIndex = if (sourceIndex <= destinationIndex) sourceIndex else destinationIndex
            val endIndex = if (sourceIndex <= destinationIndex) destinationIndex else sourceIndex

            items(endIndex - startIndex + 1) { index ->
                val stopIndex = startIndex + index
                val stop = journey.route.stops[stopIndex]
                val isFirst = stopIndex == startIndex
                val isLast = stopIndex == endIndex

                BusStopListItem(
                    stop = stop,
                    index = index + 1,
                    isFirst = isFirst,
                    isLast = isLast
                )
            }
        }
    }
}

@Composable
fun BusStopListItem(
    stop: BusStop,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Left timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Stop number indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isFirst -> DelhiOrange
                            isLast -> DelhiOrange
                            else -> DelhiOrange.copy(alpha = 0.6f)
                        }
                    )
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Connecting line if not last station
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(DelhiOrange.copy(alpha = 0.5f))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 4.dp)
        ) {
            // Stop details
            Text(
                text = stop.stopName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isFirst || isLast) FontWeight.Bold else FontWeight.Normal,
                color = if (isFirst || isLast)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = when {
                    isFirst -> "Departure"
                    isLast -> "Arrival"
                    else -> "Stop"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isFirst -> DelhiOrange
                    isLast -> DelhiOrange
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isFirst || isLast) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun BusWelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = DelhiOrange.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Plan Your Bus Journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select your starting stop and destination above to find the best bus routes.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Delhi Bus Network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BusNetworkStat(
                            label = "Routes",
                            value = "580+",
                            icon = Icons.Default.DirectionsBus
                        )

                        BusNetworkStat(
                            label = "Stops",
                            value = "3,500+",
                            icon = Icons.Default.Place
                        )

                        BusNetworkStat(
                            label = "Buses",
                            value = "6,000+",
                            icon = Icons.Default.AltRoute
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusNetworkStat(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DelhiOrange.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DelhiOrange,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// Helper function to get bus type description
fun getBusType(routeType: Int): String {
    return when (routeType) {
        0 -> "Tram"
        1 -> "Subway"
        2 -> "Rail"
        3 -> "Bus"
        4 -> "Ferry"
        5 -> "Cable Car"
        6 -> "Gondola"
        7 -> "Funicular"
        else -> "Bus"
    }
}