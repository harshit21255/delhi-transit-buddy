package com.example.delhitransit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.delhitransit.data.local.entity.StationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM stations ORDER BY line, stationId")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE line = :lineName ORDER BY stationId")
    fun getStationsByLine(lineName: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE name LIKE '%' || :query || '%'")
    fun searchStations(query: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE name = :name LIMIT 1")
    suspend fun getStationByName(name: String): StationEntity?

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun getStationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)

    @Query("DELETE FROM stations")
    suspend fun deleteAllStations()
}