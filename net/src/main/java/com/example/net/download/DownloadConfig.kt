package com.example.net.download

data class DownloadConfig(
    val url: String,
    val saveDir: String,
    val fileName: String? = null,
    val threadNum: Int = 3
)
