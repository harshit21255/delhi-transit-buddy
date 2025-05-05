package com.example.delhitransit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        GradientTextSimple(
                            text = "Delhi Transit",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
//            // Hero card with app logo
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                shape = RoundedCornerShape(24.dp),
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                )
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(24.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(100.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.background),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.delhi_metro_logo),
//                            contentDescription = "Delhi Metro Logo",
//                            modifier = Modifier
//                                .size(80.dp)
//                                .clip(CircleShape),
//                            contentScale = ContentScale.Fit
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "Open Delhi Transit",
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = "Navigate Delhi with ease",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
//                    )
//                }
//            }

            // Section title
            Text(
                text = "SELECT MODE OF TRAVEL",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Main action buttons
            TransitActionCard(
                icon = Icons.Default.Train,
                title = "Metro Routes",
                description = "Find the best metro route between stations",
                onClick = { navController.navigate("metro") }
            )

            TransitActionCard(
                icon = Icons.Default.DirectionsBus,
                title = "Bus Routes",
                description = "Find the best bus route between stops",
                onClick = { navController.navigate("real_time_transit") }
            )

//            TransitActionCard(
//                icon = Icons.Default.Map,
//                title = "Live Transit Map",
//                description = "View transit vehicles on an interactive map",
//                onClick = { navController.navigate("live_bus_map") }
//            )

//            // Quick actions section
//            Text(
//                text = "USEFUL TOOLS",
//                style = MaterialTheme.typography.labelLarge,
//                fontWeight = FontWeight.Bold,
//                letterSpacing = 1.sp,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//            )
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                SmallActionCard(
//                    icon = Icons.Default.Train,
//                    title = "Metro Info",
//                    modifier = Modifier.weight(1f),
//                    onClick = { navController.navigate("metro") }
//                )
//
//                SmallActionCard(
//                    icon = Icons.Default.DirectionsBus,
//                    title = "Bus Tracking",
//                    modifier = Modifier.weight(1f),
//                    onClick = { navController.navigate("real_time_transit") }
//                )
//
//                SmallActionCard(
//                    icon = Icons.Default.Info,
//                    title = "About",
//                    modifier = Modifier.weight(1f),
//                    onClick = { /* Show about dialog */ }
//                )
//            }

            // App info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "About Delhi Transit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A comprehensive app designed to help residents and visitors navigate Delhi's extensive public transportation network.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Data provided by DMRC, DTC, IIITD & DIMTS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TransitActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Accent color tab on the left side
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(accentColor)
                    .align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    accentColor,
                                    accentColor.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Animated button with circular shape
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
