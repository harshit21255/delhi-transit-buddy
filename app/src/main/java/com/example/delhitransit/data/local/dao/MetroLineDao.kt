package com.example.delhitransit.data.local.dao

import androidx.room.*
import com.example.delhitransit.data.local.entity.MetroLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetroLineDao {
    @Query("SELECT * FROM metro_lines")
    fun getAllLines(): Flow<List<MetroLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<MetroLineEntity>)
}