package com.example.delhitransit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.delhitransit.data.local.dao.*
import com.example.delhitransit.data.local.entity.*

@Database(
    entities = [
        StationEntity::class,
        MetroLineEntity::class,
        BusAgencyEntity::class,
        BusRouteEntity::class,
        BusStopEntity::class,
        BusTripEntity::class,
        BusStopSequenceEntity::class,
        StopTimeEntity::class,
        CombinedBusDataEntity::class
    ],
    version = 1
)
abstract class MetroDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun metroLineDao(): MetroLineDao
    abstract fun busAgencyDao(): BusAgencyDao
    abstract fun busRouteDao(): BusRouteDao
    abstract fun busStopDao(): BusStopDao
    abstract fun busTripDao(): BusTripDao
    abstract fun busStopSequenceDao(): BusStopSequenceDao
    abstract fun stopTimeDao(): StopTimeDao
    abstract fun combinedBusDataDao(): BusRouteTripDao

    companion object {
        @Volatile
        private var INSTANCE: MetroDatabase? = null

        fun getDatabase(context: Context): MetroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetroDatabase::class.java,
                    "metro_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}