package com.example.gyro.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.db.AppDatabase
import com.example.db.GyroData
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private const val TAG = "GyroSensor"

@Composable
fun GyroSensorComposable() {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val gyroscope = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }
    
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = remember { db.gyroDao() }

    var ax by remember { mutableStateOf(0f) }
    var ay by remember { mutableStateOf(0f) }
    var az by remember { mutableStateOf(0f) }
    var gx by remember { mutableStateOf(0f) }
    var gy by remember { mutableStateOf(0f) }
    var gz by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting sensor data recording loop")
            while (isActive) {
                try {
                    val timestamp = System.currentTimeMillis() * 1000
                    dao.insert(
                        GyroData(
                            ax = ax, ay = ay, az = az,
                            gx = gx, gy = gy, gz = gz,
                            timestamp = timestamp
                        )
                    )
                    Log.v(TAG, "Inserted gyro data at $timestamp")
                } catch (e: Exception) {
                    Log.e(TAG, "Error inserting data", e)
                }
                delay(100)
            }
        }
    }

    DisposableEffect(accelerometer, gyroscope) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        ax = event.values[0]; ay = event.values[1]; az = event.values[2]
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gx = event.values[0]; gy = event.values[1]; gz = event.values[2]
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        accelerometer?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Accelerometer")
        Text(text = "x: $ax")
        Text(text = "y: $ay")
        Text(text = "z: $az")
        Text(text = "Gyroscope")
        Text(text = "x: $gx")
        Text(text = "y: $gy")
        Text(text = "z: $gz")
    }
}