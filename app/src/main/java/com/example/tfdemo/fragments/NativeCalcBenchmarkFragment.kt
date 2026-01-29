package com.example.tfdemo.fragments

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nativecalc.NativeCalc
import com.example.nativecalc.Logger
import com.example.tfdemo.R
import kotlin.random.Random

class NativeCalcBenchmarkFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_native_calc_benchmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnRun: Button = view.findViewById(R.id.btn_run_benchmark)
        val tvResult: TextView = view.findViewById(R.id.tv_benchmark_result)

        btnRun.setOnClickListener {
            val javaCount = 10_000
            val nativeSize = 1_000

            // Java层：随机10000次乘法+求和
            var javaSum = 0.0
            val tJavaStart = SystemClock.elapsedRealtimeNanos()
            repeat(javaCount) {
                val a = Random.nextInt(1, 1000)
                val b = Random.nextInt(1, 1000)
                javaSum += a * b
            }
            val tJavaCostMs = (SystemClock.elapsedRealtimeNanos() - tJavaStart) / 1_000_000.0

            // C++层：随机1000个数字求和（并做一次乘法）
            val arr = FloatArray(nativeSize) { Random.nextFloat() * 1000f }
            val tNativeStart = SystemClock.elapsedRealtimeNanos()
            val nativeSum = NativeCalc.sum(arr)
            val nativeMul = NativeCalc.multiply(Random.nextInt(1, 1000), Random.nextInt(1, 1000))
            val tNativeCostMs = (SystemClock.elapsedRealtimeNanos() - tNativeStart) / 1_000_000.0

            // 回调示例：从C++调用Java方法
            NativeCalc.callJavaLog(object : Logger {
                override fun log(msg: String) { android.util.Log.i("NativeCalc", msg) }
            }, "Benchmark done")

            tvResult.text = buildString {
                appendLine("Java(10000 mul+sum): ${"%.3f".format(tJavaCostMs)} ms, sum=${"%.2f".format(javaSum)}")
                appendLine("Native(1000 sum + 1 mul): ${"%.3f".format(tNativeCostMs)} ms, sum=${"%.2f".format(nativeSum)}, mul=$nativeMul")
            }
        }
    }
}
