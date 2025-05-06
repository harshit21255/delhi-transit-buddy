package com.example.delhitransit.data.model

data class BusAgency(
    val agencyId: String,
    val agencyName: String,
    val agencyUrl: String,
    val agencyTimezone: String
)

data class BusRoute(
    val routeId: String,
    val agencyId: String,
    val routeShortName: String,
    val routeLongName: String,
    val routeType: Int
)

data class BusStop(
    val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double
)

data class BusTrip(
    val tripId: String,
    val routeId: String,
    val serviceId: String
)

data class BusRouteWithStops(
    val route: BusRoute,
    val stops: List<BusStop>,
    val startStop: BusStop,
    val endStop: BusStop,
)

data class BusJourney(
    val source: BusStop,
    val destination: BusStop,
    val route: BusRouteWithStops,
    val totalStops: Int
)