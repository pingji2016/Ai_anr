package com.example.nativecalc

object NativeCalc {
    init { System.loadLibrary("nativecalc") }
    external fun multiply(a: Int, b: Int): Int
    external fun sum(values: FloatArray): Float
    external fun callJavaLog(logger: Logger, msg: String)
}
