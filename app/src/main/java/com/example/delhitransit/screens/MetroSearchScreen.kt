package com.example.delhitransit.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.delhitransit.R
import com.example.delhitransit.data.model.Route
import com.example.delhitransit.data.model.Station
import com.example.delhitransit.ui.theme.DelhiBlue
import com.example.delhitransit.ui.theme.DelhiRed
import com.example.delhitransit.viewmodel.MetroJourneyViewModel
import com.example.delhitransit.viewmodel.MetroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroScreen(
    navController: NavController,
    metroViewModel: MetroViewModel = hiltViewModel(),
    journeyViewModel: MetroJourneyViewModel = hiltViewModel()
) {
    val searchResults by metroViewModel.searchResults.collectAsState()
    val route by metroViewModel.route.collectAsState()
    val error by metroViewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var sourceStation by remember { mutableStateOf("") }
    var destinationStation by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf<String?>(null) }

    // When a route is found, immediately set it in the JourneyViewModel
    LaunchedEffect(route) {
        route?.let {
            journeyViewModel.setJourney(it)
        }
    }

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            metroViewModel.clearError()
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
                                DelhiBlue,
                                DelhiBlue.copy(alpha = 0.8f)
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
                            text = "Delhi Metro",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Redesigned search fields
                    JourneySearchCard(
                        sourceStation = sourceStation,
                        destinationStation = destinationStation,
                        onSourceClick = { selectionMode = "source" },
                        onDestinationClick = { selectionMode = "destination" },
                        onFindRouteClick = {
                            if (sourceStation.isNotEmpty() && destinationStation.isNotEmpty()) {
                                metroViewModel.findRoute(sourceStation, destinationStation)
                            }
                        }

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
            // Selection mode indicator
            if (selectionMode != null) {
                SearchModeHeader(
                    selectionMode = selectionMode!!,
                    onCancelClick = { selectionMode = null }
                )

                // Search Station
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        metroViewModel.searchStations(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Search for station") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                metroViewModel.searchStations("")
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                        items(searchResults) { station ->
                            EnhancedStationItem(
                                station = station,
                                onClick = {
                                    when (selectionMode) {
                                        "source" -> sourceStation = station.name
                                        "destination" -> destinationStation = station.name
                                    }
                                    selectionMode = null
                                    searchQuery = ""
                                    metroViewModel.clearResults()
                                }
                            )
                        }
                    }
                } else if (searchQuery.isNotEmpty()) {
                    // No results found
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
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No stations found",
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
            } else if (route != null) {
                // Show route details
                EnhancedRouteDetails(
                    route = route!!,
                    onBeginJourney = {
                        navController.navigate("journey_tracking")
                    }
                )
            } else {
                // Welcome screen when no route or search is active
                MetroWelcomeScreen()
            }
        }
    }
}

@Composable
fun JourneySearchCard(
    sourceStation: String,
    destinationStation: String,
    onSourceClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onFindRouteClick: () -> Unit
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
                value = sourceStation,
                onValueChange = { /* Readonly */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSourceClick),
                readOnly = true,
                label = { Text("From") },
                placeholder = { Text("Select departure station") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "From",
                        tint = DelhiBlue
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = DelhiBlue,
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
                        contentDescription = "Swap stations",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Destination field
            OutlinedTextField(
                value = destinationStation,
                onValueChange = { /* Readonly */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDestinationClick),
                readOnly = true,
                label = { Text("To") },
                placeholder = { Text("Select destination station") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "To",
                        tint = DelhiRed
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = DelhiRed,
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
                enabled = sourceStation.isNotEmpty() && destinationStation.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
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
    onCancelClick: () -> Unit
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
            val iconTint = if (selectionMode == "source") DelhiBlue else DelhiRed

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Select ${if (selectionMode == "source") "departure" else "destination"} station",
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
fun EnhancedStationItem(
    station: Station,
    onClick: () -> Unit
) {
    val lineColor = getLineColor(station.line)

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
            // Line color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(lineColor)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // First letter of line name
                Text(
                    text = station.line.first().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = getLineName(station.line),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EnhancedRouteDetails(
    route: Route,
    onBeginJourney: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            // Route summary card
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
                            painter = painterResource(id = R.drawable.ic_metro),
                            contentDescription = null,
                            tint = DelhiBlue,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Your Journey",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${route.totalStations+1} stations • ${route.interchangeCount} ${if (route.interchangeCount == 1) "change" else "changes"}",
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
                        // Start station
                        JourneyEndpoint(
                            icon = Icons.Default.Place,
                            label = "From",
                            stationName = route.source.name,
                            lineName = getLineName(route.source.line),
                            lineColor = getLineColor(route.source.line),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(horizontal = 8.dp)
                        )

                        // End station
                        JourneyEndpoint(
                            icon = Icons.Default.LocationOn,
                            label = "To",
                            stationName = route.destination.name,
                            lineName = getLineName(route.destination.line),
                            lineColor = getLineColor(route.destination.line),
                            modifier = Modifier.weight(1f),
                            alignment = Alignment.End
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onBeginJourney,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DelhiRed
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
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

            // Journey steps header
            Text(
                text = "JOURNEY DETAILS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
            )
        }

        // Route path with stations
        items(route.path.size) { index ->
            val station = route.path[index]
            val isLast = index == route.path.size - 1
            val hasInterchange = !isLast && station.line != route.path[index + 1].line

            JourneyPathItem(
                station = station,
                index = index + 1,
                isLast = isLast,
                hasInterchange = hasInterchange,
                nextLine = if (hasInterchange) route.path[index + 1].line else null
            )
        }
    }
}

@Composable
fun JourneyEndpoint(
    icon: ImageVector,
    label: String,
    stationName: String,
    lineName: String,
    lineColor: Color,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (alignment == Alignment.End) Arrangement.End else Arrangement.Start
        ) {
            if (alignment == Alignment.Start) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (label == "From") DelhiBlue else DelhiRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (alignment == Alignment.End) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (label == "From") DelhiBlue else DelhiRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stationName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = if (alignment == Alignment.End) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            color = lineColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = lineName,
                style = MaterialTheme.typography.bodySmall,
                color = lineColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun JourneyPathItem(
    station: Station,
    index: Int,
    isLast: Boolean,
    hasInterchange: Boolean,
    nextLine: String?
) {
    val lineColor = getLineColor(station.line)
    val nextLineColor = if (nextLine != null) getLineColor(nextLine) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Station number indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(lineColor)
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
                        .height(if (hasInterchange) 40.dp else 24.dp)
                        .background(if (hasInterchange) lineColor else lineColor.copy(alpha = 0.5f))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 4.dp)
        ) {
            // Station details
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = getLineName(station.line),
                style = MaterialTheme.typography.bodyMedium,
                color = lineColor,
                fontWeight = FontWeight.Medium
            )

            // Interchange information
            if (hasInterchange && nextLine != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_interchange),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Change to ${getLineName(nextLine)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(nextLineColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetroWelcomeScreen() {
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
                painter = painterResource(id = R.drawable.ic_metro),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Plan Your Metro Journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select your starting station and destination above to find the best route.",
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
                        text = "Delhi Metro Network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetroNetworkStat(
                            label = "Lines",
                            value = "11",
                            icon = Icons.Default.Train
                        )

                        MetroNetworkStat(
                            label = "Stations",
                            value = "285",
                            icon = Icons.Default.Place
                        )

                        MetroNetworkStat(
                            label = "Length",
                            value = "390 km",
                            icon = Icons.Default.Straighten
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetroNetworkStat(
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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

// Line color function - reused from your existing code but enhanced
fun getLineColor(line: String): Color {
    return when (line.lowercase()) {
        "yellow" -> Color(0xFFFFD700)
        "blue" -> Color(0xFF1976D2)   // Enhanced blue for better visibility
        "red" -> Color(0xFFD32F2F)
        "green" -> Color(0xFF4CAF50)
        "violet" -> Color(0xFF8E24AA)
        "orange" -> Color(0xFFFF9800)
        "magenta" -> Color(0xFFE91E63)
        "pink" -> Color(0xFFFF80AB)
        "aqua" -> Color(0xFF00BCD4)
        "grey" -> Color(0xFF757575)
        "rapid" -> Color(0xFF4682B4)
        "greenbranch" -> Color(0xFF388E3C)
        "bluebranch" -> Color(0xFF1565C0)
        "pinkbranch" -> Color(0xFFEC407A)
        else -> Color.Gray
    }
}

// Function to get line name - reused from your existing code
fun getLineName(line: String): String {
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