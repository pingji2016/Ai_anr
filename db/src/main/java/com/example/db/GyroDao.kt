package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GyroDao {
    @Insert
    suspend fun insert(gyroData: GyroData)

    @Insert
    suspend fun insertAll(gyroDataList: List<GyroData>)

    @Query("SELECT * FROM gyro_data ORDER BY timestamp DESC")
    fun getAll(): Flow<List<GyroData>>

    @Query("DELETE FROM gyro_data")
    suspend fun deleteAll()
}
