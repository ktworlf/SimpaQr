package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScanRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scans WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteScans(): Flow<List<ScanRecord>>

    @Query("SELECT COUNT(*) FROM scans")
    fun getTotalScansCount(): Flow<Int>

    @Query("SELECT * FROM scans WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): ScanRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanRecord): Long

    @Update
    suspend fun updateScan(scan: ScanRecord)

    @Delete
    suspend fun deleteScan(scan: ScanRecord)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteScanById(id: Long)

    @Query("DELETE FROM scans")
    suspend fun clearAllScans()
}
