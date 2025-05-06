// BusEntity.kt (new file)
package com.example.delhitransit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bus_agencies")
data class BusAgencyEntity(
    @PrimaryKey
    val agencyId: String,
    val agencyName: String,
    val agencyUrl: String,
    val agencyTimezone: String
)

@Entity(tableName = "bus_routes")
data class BusRouteEntity(
    @PrimaryKey
    val routeId: String,
    val agencyId: String,
    val routeShortName: String,
    val routeLongName: String,
    val routeType: Int,
)

@Entity(tableName = "bus_stops")
data class BusStopEntity(
    @PrimaryKey
    val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double
)

@Entity(tableName = "bus_trips")
data class BusTripEntity(
    @PrimaryKey
    val tripId: String,
    val routeId: String,
    val serviceId: String
)

@Entity(tableName = "bus_stop_sequences")
data class BusStopSequenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: String,
    val stopId: String,
    val stopSequence: Int
)

@Entity(
    tableName = "bus_stop_times",
    primaryKeys = ["tripId", "stopSequence"],
    indices = [Index("stopId"), Index("tripId")]
)
data class StopTimeEntity(
    val tripId: String,
    val stopId: String,
    val arrivalTime: String,
    val departureTime: String,
    val stopSequence: Int
)

@Entity(tableName = "combined_bus_data")
data class CombinedBusDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val busId: String,
    val routeId: String,
    val routeName: String,
    val stopId: String,
    val stopName: String,
    val stopSequence: Int
)