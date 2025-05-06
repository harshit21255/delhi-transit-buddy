package com.example.delhitransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delhitransit.screens.BusJourneyTrackingScreen
import com.example.delhitransit.screens.BusScreen
import com.example.delhitransit.screens.LandingPage
import com.example.delhitransit.screens.MetroScreen
import com.example.delhitransit.ui.theme.DelhiTransitTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.delhitransit.screens.JourneyTrackingScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dalvikVMHeap = Class.forName("dalvik.system.VMRuntime")
            .getMethod("getRuntime")
            .invoke(null)
        dalvikVMHeap.javaClass.getMethod("setTargetHeapUtilization", Float::class.java)
            .invoke(dalvikVMHeap, 0.75f)
        enableEdgeToEdge()
        setContent {
            DelhiTransitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}


@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val navigationItems = listOf(
        NavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            route = "home"
        ),
        NavigationItem(
            title = "Metro",
            selectedIcon = Icons.Filled.Train,
            route = "metro"
        ),
        NavigationItem(
            title = "Bus",
            selectedIcon = Icons.Filled.DirectionsBus,
            route = "bus"
        )
    )

    Scaffold(

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                LandingPage(navController = navController)
            }
            composable("metro") {
                MetroScreen(navController = navController)
            }
            composable("bus") {
                BusScreen(navController = navController)
            }
            composable("journey_tracking") {
                JourneyTrackingScreen(navController = navController)
            }
            composable("bus_journey_tracking") {
                BusJourneyTrackingScreen(navController = navController)
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)