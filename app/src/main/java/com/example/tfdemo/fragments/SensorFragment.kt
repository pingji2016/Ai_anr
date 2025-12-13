package com.example.tfdemo.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfdemo.databinding.FragmentSensorBinding

class SensorFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        if (accelerometer == null) {
            binding.tvAccelX.text = "x: -"
            binding.tvAccelY.text = "y: -"
            binding.tvAccelZ.text = "z: -"
        }
        if (gyroscope == null) {
            binding.tvGyroX.text = "x: -"
            binding.tvGyroY.text = "y: -"
            binding.tvGyroZ.text = "z: -"
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                binding.tvAccelX.text = "x: ${event.values[0]}"
                binding.tvAccelY.text = "y: ${event.values[1]}"
                binding.tvAccelZ.text = "z: ${event.values[2]}"
            }
            Sensor.TYPE_GYROSCOPE -> {
                binding.tvGyroX.text = "x: ${event.values[0]}"
                binding.tvGyroY.text = "y: ${event.values[1]}"
                binding.tvGyroZ.text = "z: ${event.values[2]}"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
