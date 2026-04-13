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
import kotlinx.coroutines.launch

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
    
    // Internal refs to store latest sensor data without triggering recomposition
    val latestAx = remember { mutableFloatStateOf(0f) }
    val latestAy = remember { mutableFloatStateOf(0f) }
    val latestAz = remember { mutableFloatStateOf(0f) }
    val latestGx = remember { mutableFloatStateOf(0f) }
    val latestGy = remember { mutableFloatStateOf(0f) }
    val latestGz = remember { mutableFloatStateOf(0f) }

    // UI states updated at a lower frequency (e.g. 5Hz) to save power/CPU
    var displayAx by remember { mutableFloatStateOf(0f) }
    var displayAy by remember { mutableFloatStateOf(0f) }
    var displayAz by remember { mutableFloatStateOf(0f) }
    var displayGx by remember { mutableFloatStateOf(0f) }
    var displayGy by remember { mutableFloatStateOf(0f) }
    var displayGz by remember { mutableFloatStateOf(0f) }

    val buffer = remember { mutableListOf<GyroData>() }
    var lastFlushTs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting sensor data recording loop")
            while (isActive) {
                try {
                    val tsMs = System.currentTimeMillis()
                    val timestamp = tsMs * 1000
                    
                    val data = GyroData(
                        ax = latestAx.floatValue, ay = latestAy.floatValue, az = latestAz.floatValue,
                        gx = latestGx.floatValue, gy = latestGy.floatValue, gz = latestGz.floatValue,
                        timestamp = timestamp
                    )
                    buffer.add(data)

                    // Update UI display values at a controlled rate (roughly every 200ms)
                    withContext(Dispatchers.Main) {
                        displayAx = data.ax; displayAy = data.ay; displayAz = data.az
                        displayGx = data.gx; displayGy = data.gy; displayGz = data.gz
                    }

                    val needFlush = buffer.size >= 20 || (tsMs - lastFlushTs) >= 1000
                    if (needFlush) {
                        val flushList = buffer.toList()
                        buffer.clear()
                        dao.insertAll(flushList)
                        lastFlushTs = tsMs
                        Log.v(TAG, "Flushed ${flushList.size} items to DB")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in recording loop", e)
                }
                delay(100) // Record every 100ms
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (buffer.isNotEmpty()) {
                val remaining = buffer.toList()
                buffer.clear()
                // Use GlobalScope as a last resort to ensure data is saved after composition is destroyed
                @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    try {
                        dao.insertAll(remaining)
                        Log.v(TAG, "Final flush of ${remaining.size} items on dispose")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed final flush", e)
                    }
                }
            }
        }
    }

    DisposableEffect(accelerometer, gyroscope) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        latestAx.floatValue = event.values[0]
                        latestAy.floatValue = event.values[1]
                        latestAz.floatValue = event.values[2]
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        latestGx.floatValue = event.values[0]
                        latestGy.floatValue = event.values[1]
                        latestGz.floatValue = event.values[2]
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Accelerometer (Update Rate: 5Hz)")
        Text(text = "x: ${"%.2f".format(displayAx)}")
        Text(text = "y: ${"%.2f".format(displayAy)}")
        Text(text = "z: ${"%.2f".format(displayAz)}")
        Text(text = "Gyroscope")
        Text(text = "x: ${"%.2f".format(displayGx)}")
        Text(text = "y: ${"%.2f".format(displayGy)}")
        Text(text = "z: ${"%.2f".format(displayGz)}")
    }
}