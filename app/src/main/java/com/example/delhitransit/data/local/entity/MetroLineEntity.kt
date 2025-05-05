package com.example.delhitransit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "metro_lines")
data class MetroLineEntity(
    @PrimaryKey
    val name: String,
    val color: String,
    val totalStations: Int
)