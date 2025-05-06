package com.example.delhitransit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.delhitransit.data.local.entity.BusAgencyEntity
import com.example.delhitransit.data.local.entity.BusRouteEntity
import com.example.delhitransit.data.local.entity.BusStopEntity
import com.example.delhitransit.data.local.entity.BusStopSequenceEntity
import com.example.delhitransit.data.local.entity.BusTripEntity
import com.example.delhitransit.data.local.entity.CombinedBusDataEntity
import com.example.delhitransit.data.local.entity.StopTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusAgencyDao {
    @Query("SELECT * FROM bus_agencies")
    fun getAllAgencies(): Flow<List<BusAgencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgencies(agencies: List<BusAgencyEntity>)
}

@Dao
interface BusRouteDao {
    @Query("SELECT * FROM bus_routes")
    fun getAllRoutes(): Flow<List<BusRouteEntity>>

    @Query("SELECT * FROM bus_routes WHERE routeId = :routeId")
    suspend fun getRouteById(routeId: String): BusRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<BusRouteEntity>)

    @Query("SELECT COUNT(*) FROM bus_routes")
    suspend fun getRouteCount(): Int
}

@Dao
interface BusStopDao {
    @Query("SELECT * FROM bus_stops")
    fun getAllStops(): Flow<List<BusStopEntity>>

    @Query("SELECT * FROM bus_stops WHERE stopName LIKE '%' || :query || '%'")
    fun searchStops(query: String): Flow<List<BusStopEntity>>

    @Query("SELECT * FROM bus_stops WHERE stopId = :stopId")
    suspend fun getStopById(stopId: String): BusStopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<BusStopEntity>)

    @Query("SELECT COUNT(*) FROM bus_stops")
    suspend fun getStopCount(): Int
}

@Dao
interface BusTripDao {
    @Query("SELECT * FROM bus_trips")
    fun getAllTrips(): Flow<List<BusTripEntity>>

    @Query("SELECT * FROM bus_trips WHERE routeId = :routeId")
    fun getTripsByRouteId(routeId: String): Flow<List<BusTripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<BusTripEntity>)
}

@Dao
interface BusStopSequenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopSequences(stopSequences: List<BusStopSequenceEntity>)

    @Query("SELECT bs.* FROM bus_stops bs " +
            "INNER JOIN bus_stop_sequences bss ON bs.stopId = bss.stopId " +
            "WHERE bss.tripId = :tripId " +
            "ORDER BY bss.stopSequence")
    fun getStopsByTripId(tripId: String): Flow<List<BusStopEntity>>

    @Query("SELECT DISTINCT r.* FROM bus_routes r " +
            "INNER JOIN bus_trips t ON r.routeId = t.routeId " +
            "INNER JOIN bus_stop_sequences bss1 ON t.tripId = bss1.tripId " +
            "INNER JOIN bus_stop_sequences bss2 ON t.tripId = bss2.tripId " +
            "INNER JOIN bus_stops bs1 ON bss1.stopId = bs1.stopId " +
            "INNER JOIN bus_stops bs2 ON bss2.stopId = bs2.stopId " +
            "WHERE bs1.stopName LIKE '%' || :sourceStop || '%' " +
            "AND bs2.stopName LIKE '%' || :destStop || '%' " +
            "AND bss1.stopSequence < bss2.stopSequence")
    fun findRoutesBetweenStops(sourceStop: String, destStop: String): Flow<List<BusRouteEntity>>
}

@Dao
interface StopTimeDao {
    @Query("SELECT * FROM bus_stop_times")
    fun getAllStopTimes(): Flow<List<StopTimeEntity>>

    @Query("SELECT * FROM bus_stop_times WHERE tripId = :tripId ORDER BY stopSequence")
    fun getStopTimesByTrip(tripId: String): Flow<List<StopTimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopTimes(stopTimes: List<StopTimeEntity>)

    @Query("SELECT COUNT(*) FROM bus_stop_times")
    suspend fun getStopTimesCount(): Int

    @Query("SELECT bs.* FROM bus_stops bs " +
            "INNER JOIN bus_stop_times bst ON bs.stopId = bst.stopId " +
            "WHERE bst.tripId = :tripId " +
            "ORDER BY bst.stopSequence")
    fun getStopsByTripIdOrdered(tripId: String): Flow<List<BusStopEntity>>

    @Query("SELECT DISTINCT r.* FROM bus_routes r " +
            "INNER JOIN bus_trips t ON r.routeId = t.routeId " +
            "INNER JOIN bus_stop_times bst1 ON t.tripId = bst1.tripId " +
            "INNER JOIN bus_stop_times bst2 ON t.tripId = bst2.tripId " +
            "INNER JOIN bus_stops bs1 ON bst1.stopId = bs1.stopId " +
            "INNER JOIN bus_stops bs2 ON bst2.stopId = bs2.stopId " +
            "WHERE bs1.stopName LIKE '%' || :sourceStop || '%' " +
            "AND bs2.stopName LIKE '%' || :destStop || '%' " +
            "AND bst1.stopSequence < bst2.stopSequence")
    fun findRoutesBetweenStops(sourceStop: String, destStop: String): Flow<List<BusRouteEntity>>
}

@Dao
interface BusRouteTripDao {
    @Query("SELECT * FROM combined_bus_data")
    fun getAllCombinedData(): Flow<List<CombinedBusDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombinedData(data: List<CombinedBusDataEntity>)

    @Query("SELECT DISTINCT stopName FROM combined_bus_data ORDER BY stopName")
    fun getAllStopNames(): Flow<List<String>>

    @Query("SELECT * FROM combined_bus_data WHERE stopName LIKE '%' || :query || '%'")
    fun searchStopsByName(query: String): Flow<List<CombinedBusDataEntity>>
}