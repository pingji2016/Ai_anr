package com.example.net.download

import java.io.Serializable

data class DownloadInfo(
    val url: String,
    var fileName: String = "",
    var totalLength: Long = 0,
    var currentLength: Long = 0,
    val threads: MutableList<ThreadInfo> = mutableListOf()
) : Serializable

data class ThreadInfo(
    val id: Int,
    val start: Long,
    val end: Long,
    var current: Long
) : Serializable
