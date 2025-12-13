package com.example.tfdemo.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.tfdemo.ui.theme.TfDemoTheme
import com.example.gyro.ui.GyroSensorComposable

class SensorFragment : Fragment() {
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TfDemoTheme {
                    GyroSensorComposable()
                }
            }
        }
    }
}