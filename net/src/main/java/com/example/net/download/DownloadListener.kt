package com.example.net.download

interface DownloadListener {
    fun onStart(url: String, totalLength: Long)
    fun onProgress(url: String, progress: Int, currentLength: Long, speed: Long)
    fun onSuccess(url: String, path: String)
    fun onPause(url: String)
    fun onFail(url: String, msg: String)
}
