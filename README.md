# Delhi Transit


## Overview

Delhi Transit is a comprehensive public transportation companion app for Delhi, India, that helps users navigate the city's extensive metro and bus networks. It provides real-time journey planning, navigation assistance, and an intuitive interface for finding the best routes across Delhi's public transport system.

## Features

### Metro Navigation
- **Station Search**: Easily find any metro station in Delhi using the search functionality
- **Route Planning**: Get optimal routes between any two metro stations with detailed interchange information
- **Real-time Journey Tracking**: Track your journey in real-time with location-based updates
- **Station Proximity Alerts**: Receive notifications when approaching your destination or interchange stations
- **Comprehensive Network Coverage**: Includes all Delhi Metro lines with accurate station information obtained from DMRC Open Transit Data.

### Bus Navigation
- **Bus Stop Search**: Find any bus stop in Delhi using the search functionality
- **Route Planning**: Get direct routes or multi-leg journeys between any two bus stops
- **Journey Tracking**: Track your bus journey with real-time location updates
- **Approaching Stop Alerts**: Get notified when your destination stop is approaching
- **Complete GTFS Integration**: Uses Delhi's GTFS (General Transit Feed Specification) data for accurate information

### User Experience
- **Beautiful UI**: Modern Material Design 3 interface with intuitive navigation
- **Dark Mode Support**: Full dark mode support for comfortable use in low-light conditions
- **Customized Branding**: DMRC-specific theming with appropriate colors matching the city's transit system
- **Offline Capability**: Core functionality works offline once data is downloaded

## Architecture

Delhi Transit is built with modern Android development practices:

### Technical Foundation
- **Language**: 100% Kotlin
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose for modern, declarative UI
- **Dependency Injection**: Hilt for dependency management
- **Concurrency**: Kotlin Coroutines and Flow for asynchronous operations
- **Database**: Room for local data persistence
- **Navigation**: Jetpack Navigation for seamless screen transitions

### Data Layer
- **Repositories**: Clean separation of concerns with repository pattern
- **Data Models**: Type-safe models for all entities
- **Local Storage**: Room database for storing transit information
- **GTFS Integration**: Custom parsers for GTFS data formats

### Key Components

#### Metro System
- **Station Management**: Complete database of all Delhi Metro stations
- **Line Information**: All metro lines with accurate color coding and interchange information
- **Path Finding**: Dijkstra's algorithm implementation for optimal route finding
- **Interchange Handling**: Smart handling of station interchanges with transfer penalties

#### Bus System
- **GTFS Processing**: Custom GTFS data processor for Delhi's bus network
- **Stop Management**: Database of all bus stops across Delhi
- **Route Finding**: Direct and graph-based route finding algorithms
- **Combined Data**: Efficient combined data structures for fast queries

## Project Requirements

This application was developed to fulfill the following requirements:

### Core Requirements
- **Multiple Activities/Fragments**: The app implements multiple activity fragments including the home screen, metro journey planner, bus journey planner, and journey tracking screens
- **Network and Database Connectivity**: Full integration with Room database for local storage of transit data and network connectivity for potential future real-time updates
- **Data Caching**: Efficient caching of GTFS and metro data in local database to enable offline functionality
- **Background Services**: Location tracking service that runs in the background during journey navigation
- **Error Handling**: Comprehensive error messaging and graceful degradation when network or other services are unavailable

### Advanced Features
1. **Native API Integration**
   - Integration with Android's location services API
   - Full implementation of notification services for station/stop proximity alerts
   - Usage of system services for database and asset management

2. **Sensing Capabilities**
   - GPS location tracking for real-time journey progress
   - Proximity detection to metro stations and bus stops
   - Geofencing to trigger notifications when approaching destinations

3. **Energy-Efficient Features**
   - Smart location tracking intervals to minimize battery usage
   - Batch processing of large GTFS datasets to reduce memory and CPU consumption
   - Efficient database queries with proper indexing
   - On-demand resource loading to minimize energy consumption

4. **Unique User Interface**
   - Custom-designed journey tracking interface with color-coded metro lines
   - Interactive route visualization with transfer points
   - Animated journey progress indicators
   - Material Design 3 implementation with Delhi Transit-specific theming
   - Custom icons and app logo used

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/delhitransit/
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room database and DAOs
│   │   │   │   ├── model/          # Data models
│   │   │   │   └── repository/     # Repositories
│   │   │   ├── di/                 # Dependency injection modules
│   │   │   ├── screens/            # UI screens using Compose
│   │   │   ├── ui/                 # UI components and themes
│   │   │   ├── util/               # Utilities and helpers
│   │   │   └── viewmodel/          # ViewModels
│   │   ├── assets/
│   │   │   ├── DMRC_STATIONS.json  # Metro station data
│   │   │   └── GTFS/               # Bus GTFS data files
│   │   └── res/                    # Resources
└── build.gradle                    # Build configuration
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 24 or higher
- Kotlin 1.9.0 or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/harshit21255/delhi-transit-buddy.git
   ```
2. Add ```GTFS/stop_times.txt``` from OTD static data in ```app/src/main/assets/GTFS/```
3. Open the project in Android Studio
4. Sync the project with Gradle files
5. Run the app on an emulator or physical device

## Usage Guide

### Finding a Metro Route
1. Tap the "Metro" tab in the bottom navigation
2. Select your departure station by tapping "From"
3. Select your destination station by tapping "To"
4. Tap "Find Route" to see the optimal journey
5. Review the route details including interchanges and estimated time
6. Tap "Begin Journey" to start the real-time tracking

### Finding a Bus Route
1. Tap the "Bus" tab in the bottom navigation
2. Select your departure stop by tapping "From"
3. Select your destination stop by tapping "To"
4. Tap "Find Route" to see available bus routes
5. Select a route from the options provided
6. Tap "Begin Journey" to start the real-time tracking

### Journey Tracking
1. Enable location permissions when prompted
2. Toggle "Tracking" to start receiving proximity alerts
3. Use "Previous" and "Next" buttons to manually navigate through stops
4. View your progress in the journey tracker
5. Receive notifications when approaching your destination

## Technical Implementation

### Route Finding Algorithm
The app uses a modified Dijkstra's algorithm for both metro and bus route planning:

```kotlin
private fun findShortestPath(source: Station, destination: Station): List<Station> {
    // Initialize distances with priority queue
    val visited = mutableSetOf<Station>()
    val distances = mutableMapOf<Station, Int>()
    val previousStation = mutableMapOf<Station, Station>()
    val comparator = Comparator<Station> { s1, s2 ->
        (distances[s1] ?: Int.MAX_VALUE).compareTo(distances[s2] ?: Int.MAX_VALUE)
    }
    val queue = PriorityQueue(comparator)

    // Set initial distances
    allStationsList.forEach { station -> distances[station] = Int.MAX_VALUE }
    distances[source] = 0
    queue.add(source)

    // Main algorithm loop
    while (queue.isNotEmpty()) {
        val current = queue.poll() ?: continue
        if (current == destination) break
        if (current in visited) continue
        visited.add(current)

        // Process neighbors
        val neighbors = graph[current] ?: continue
        for (neighbor in neighbors) {
            if (neighbor !in visited) {
                // Calculate edge weight (higher for interchanges)
                val edgeWeight = if (current.line == neighbor.line) 1 else 3
                val newDistance = (distances[current] ?: Int.MAX_VALUE) + edgeWeight
                val neighborDistance = distances[neighbor] ?: Int.MAX_VALUE
                
                // Update if shorter path found
                if (newDistance < neighborDistance) {
                    distances[neighbor] = newDistance
                    previousStation[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }
    }

    // Reconstruct path
    val path = mutableListOf<Station>()
    if (previousStation.containsKey(destination)) {
        var current: Station? = destination
        while (current != null) {
            path.add(0, current)
            if (current == source) break
            current = previousStation[current]
        }
    } else if (source != destination) {
        path.add(source)
        path.add(destination)
    }

    return path
}
```

### GTFS Data Processing
The app processes GTFS data using a memory-efficient batch approach:

```kotlin
suspend fun loadStopTimesFromCsv() {
    withContext(ioDispatcher) {
        try {
            val stopTimesFile = context.assets.open("GTFS/stop_times.txt")
            val reader = BufferedReader(InputStreamReader(stopTimesFile))
            
            // Process header
            val header = reader.readLine()
            val headerColumns = header.split(",")
            val indices = extractColumnIndices(headerColumns)
            
            // Process in batches
            val batchSize = 500
            var batch = mutableListOf<StopTimeEntity>()
            var processedCount = 0
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                // Process each line and add to batch
                processStopTimeLine(line, indices, batch)
                
                // Insert batch when it reaches the batch size
                if (batch.size >= batchSize) {
                    stopTimeDao.insertStopTimes(batch)
                    processedCount += batch.size
                    batch = mutableListOf()
                    System.gc() // Help prevent OOM errors
                }
            }
            
            // Insert remaining records
            if (batch.isNotEmpty()) {
                stopTimeDao.insertStopTimes(batch)
                processedCount += batch.size
            }
            
            reader.close()
        } catch (e: Exception) {
            Log.e("BusRepository", "Error loading stop times: ${e.message}")
        }
    }
}
```

## Performance Considerations

### Memory Management
- **Batch Processing**: Large datasets are processed in batches to prevent OutOfMemoryError
- **Efficient Database Queries**: Optimized queries with proper indexing to improve performance
- **Lazy Loading**: Data is loaded only when needed to minimize memory usage

### Battery Optimization
- **Location Updates**: Location updates use an efficient interval to balance accuracy and battery life
- **Background Processing**: Minimal background processing to preserve battery life
- **Efficient Algorithms**: Optimized algorithms for route finding to reduce CPU usage

## Future Enhancements

### Planned Features
- **Fare Calculation**: Estimate journey costs based on distance and mode of transport
- **Multi-modal Journeys**: Combine metro and bus for end-to-end journey planning
- **Real-time Updates**: Integration with real-time transit data for arrivals and delays
- **Favorite Locations**: Save favorite places for quick route finding
- **Journey History**: Track and save past journeys for easy repeat trips
- **Offline Maps**: Downloadable maps for completely offline usage
- **Accessibility Features**: Enhanced support for users with disabilities

## Contributing

We welcome contributions to the Delhi Transit project! If you'd like to contribute, please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Delhi Metro Rail Corporation for station data
- GTFS data providers for Delhi bus information
- All open-source libraries used in this project

## Contact

For any questions or suggestions, please contact me at:
- Email: harshit21255@iiitd.ac.in
