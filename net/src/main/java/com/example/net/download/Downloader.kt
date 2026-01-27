package com.example.net.download

import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class Downloader(
    private val config: DownloadConfig,
    private val listener: DownloadListener?
) {

    private var status = DownloadStatus.IDLE
    private val mainHandler = Handler(Looper.getMainLooper())
    private var downloadInfo: DownloadInfo? = null
    private val executor: ExecutorService = Executors.newFixedThreadPool(config.threadNum + 1)
    private var isPaused = false
    private var isCanceled = false

    fun start() {
        if (status == DownloadStatus.DOWNLOADING) return
        status = DownloadStatus.DOWNLOADING
        isPaused = false
        isCanceled = false
        
        executor.execute {
            try {
                prepareDownload()
            } catch (e: Exception) {
                notifyFail(e.message ?: "Unknown error")
            }
        }
    }

    fun pause() {
        isPaused = true
        status = DownloadStatus.PAUSED
        notifyPause()
    }

    fun cancel() {
        isCanceled = true
        status = DownloadStatus.IDLE
        executor.shutdownNow()
    }

    private var lastProgressTime = 0L
    private var lastProgressBytes = 0L

    private fun prepareDownload() {
        var fileSize = 0L
        var fileName = config.fileName
        var acceptRanges = false

        var conn: HttpURLConnection? = null
        try {
            conn = URL(config.url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 15000
            conn.requestMethod = "HEAD"
            conn.setRequestProperty("User-Agent", "NetDownloader/1.0")
            conn.connect()

            if (conn.responseCode in arrayOf(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL)) {
                fileSize = conn.contentLengthLong
                acceptRanges = (conn.getHeaderField("Accept-Ranges")?.contains("bytes", true) == true)
            }
            // Fallback to GET if HEAD doesn't provide size
            if (fileSize <= 0) {
                conn.disconnect()
                conn = URL(config.url).openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 15000
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "NetDownloader/1.0")
                conn.connect()
                if (conn.responseCode in arrayOf(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL)) {
                    fileSize = conn.contentLengthLong
                    acceptRanges = (conn.getHeaderField("Accept-Ranges")?.contains("bytes", true) == true)
                } else {
                    notifyFail("Connection failed: ${conn.responseCode}")
                    return
                }
            }

            if (fileSize <= 0) {
                notifyFail("Invalid file size")
                return
            }

            if (fileName == null) {
                val contentDisposition = conn.getHeaderField("Content-Disposition")
                fileName = if (contentDisposition != null && contentDisposition.contains("filename=")) {
                    contentDisposition.split("filename=")[1].replace("\"", "")
                } else {
                    getFileNameFromUrl(config.url)
                }
            }

            notifyStart(fileSize)

            val file = File(config.saveDir, fileName!!)
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            RandomAccessFile(file, "rw").use { it.setLength(fileSize) }

            val threads = ArrayList<ThreadInfo>()
            val actualThreadNum = if (acceptRanges) config.threadNum.coerceAtLeast(1) else 1
            val partSize = fileSize / actualThreadNum
            for (i in 0 until actualThreadNum) {
                val start = i * partSize
                val end = if (i == actualThreadNum - 1) fileSize - 1 else (i + 1) * partSize - 1
                threads.add(ThreadInfo(i, start, end, 0))
            }

            downloadInfo = DownloadInfo(config.url, fileName!!, fileSize, 0, threads)
            lastProgressTime = System.currentTimeMillis()
            lastProgressBytes = 0

            startMultiThreadDownload(file, threads)
        } catch (e: Exception) {
            notifyFail(e.message ?: "Unknown error")
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    private fun startMultiThreadDownload(file: File, threads: List<ThreadInfo>) {
        val finishedThreads = AtomicInteger(0)
        
        for (info in threads) {
            executor.execute {
                var conn: HttpURLConnection? = null
                var raf: RandomAccessFile? = null
                var input: InputStream? = null
                
                try {
                    val startPos = info.start + info.current
                    if (startPos > info.end) {
                        if (finishedThreads.incrementAndGet() == threads.size) {
                            notifySuccess(file.absolutePath)
                        }
                        return@execute
                    }

                    val url = URL(config.url)
                    conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 15000
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Range", "bytes=$startPos-${info.end}")
                    conn.setRequestProperty("User-Agent", "NetDownloader/1.0")
                    
                    if (conn.responseCode == HttpURLConnection.HTTP_PARTIAL || conn.responseCode == HttpURLConnection.HTTP_OK) {
                        raf = RandomAccessFile(file, "rw")
                        raf.seek(startPos)
                        
                        input = conn.inputStream
                        val buffer = ByteArray(16 * 1024)
                        var len = -1
                        var retries = 0
                        val maxRetries = 3
                        while (true) {
                            len = input.read(buffer)
                            if (len == -1) break
                            if (isPaused || isCanceled) break
                            try {
                                raf.write(buffer, 0, len)
                                info.current += len

                                synchronized(this) {
                                    downloadInfo?.currentLength = downloadInfo?.currentLength?.plus(len) ?: 0
                                    val currentTime = System.currentTimeMillis()
                                    val currentBytes = downloadInfo!!.currentLength
                                    val timeDiff = currentTime - lastProgressTime
                                    val bytesDiff = currentBytes - lastProgressBytes
                                    val speed = if (timeDiff > 0) bytesDiff * 1000 / timeDiff else 0

                                    if (timeDiff >= 1000 || currentBytes == downloadInfo!!.totalLength) {
                                        val progress = if (downloadInfo!!.totalLength > 0) (currentBytes * 100 / downloadInfo!!.totalLength).toInt() else 0
                                        notifyProgress(progress, currentBytes, speed)
                                        lastProgressTime = currentTime
                                        lastProgressBytes = currentBytes
                                    }
                                }
                            } catch (writeEx: Exception) {
                                if (retries < maxRetries) {
                                    retries++
                                    Thread.sleep(500L * retries)
                                    continue
                                } else {
                                    throw writeEx
                                }
                            }
                        }
                        
                        if (!isPaused && !isCanceled) {
                            if (finishedThreads.incrementAndGet() == threads.size) {
                                notifySuccess(file.absolutePath)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!isPaused && !isCanceled) {
                         notifyFail("Thread ${info.id} error: ${e.message}")
                    }
                } finally {
                    try {
                        input?.close()
                        raf?.close()
                        conn?.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun getFileNameFromUrl(url: String): String {
        var filename = url.substring(url.lastIndexOf("/") + 1)
        if (filename.contains("?")) {
            filename = filename.substring(0, filename.indexOf("?"))
        }
        if (filename.isEmpty()) {
            filename = "unknown_file"
        }
        return filename
    }

    private fun notifyStart(totalLength: Long) {
        mainHandler.post { listener?.onStart(config.url, totalLength) }
    }

    private fun notifyProgress(progress: Int, currentLength: Long, speed: Long) {
        mainHandler.post { listener?.onProgress(config.url, progress, currentLength, speed) }
    }

    private fun notifySuccess(path: String) {
        status = DownloadStatus.COMPLETED
        mainHandler.post { listener?.onSuccess(config.url, path) }
    }

    private fun notifyPause() {
        mainHandler.post { listener?.onPause(config.url) }
    }

    private fun notifyFail(msg: String) {
        status = DownloadStatus.FAILED
        mainHandler.post { listener?.onFail(config.url, msg) }
    }
}
