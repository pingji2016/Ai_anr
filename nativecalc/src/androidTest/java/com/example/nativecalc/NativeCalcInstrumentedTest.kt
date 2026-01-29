package com.example.nativecalc

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeCalcInstrumentedTest {
    @Test
    fun testMultiply() {
        assertEquals(42, NativeCalc.multiply(6, 7))
    }

    @Test
    fun testSum() {
        assertEquals(6.0f, NativeCalc.sum(floatArrayOf(1f, 2f, 3f)), 1e-6f)
    }
}
