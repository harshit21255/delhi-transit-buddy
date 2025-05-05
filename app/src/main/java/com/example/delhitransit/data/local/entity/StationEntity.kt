package com.example.delhitransit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val line: String,
    val stationId: Int,
    val latitude: Double,
    val longitude: Double
)

