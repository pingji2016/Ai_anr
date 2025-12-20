package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gyro_data")
data class GyroData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ax: Float,
    val ay: Float,
    val az: Float,
    val gx: Float,
    val gy: Float,
    val gz: Float,
    val timestamp: Long // microseconds
)
