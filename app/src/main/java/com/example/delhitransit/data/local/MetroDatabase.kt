package com.example.delhitransit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.delhitransit.data.local.dao.MetroLineDao
import com.example.delhitransit.data.local.entity.MetroLineEntity
import com.example.delhitransit.data.local.entity.StationEntity

@Database(
    entities = [StationEntity::class, MetroLineEntity::class],
    version = 1
)
abstract class MetroDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun metroLineDao(): MetroLineDao

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
                    .createFromAsset("database/metro_database.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}