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
        // Connect to get file size
        var fileSize = 0L
        var fileName = config.fileName
        
        // ... (connection logic to get size)
        val conn = URL(config.url).openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.requestMethod = "GET"
        conn.connect()
        
        if (conn.responseCode == HttpURLConnection.HTTP_OK || conn.responseCode == HttpURLConnection.HTTP_PARTIAL) {
            fileSize = conn.contentLengthLong
             if (fileSize <= 0) {
                 notifyFail("Invalid file size")
                 return
            }
            
            if (fileName == null) {
                // Try to get filename from Content-Disposition header first
                val contentDisposition = conn.getHeaderField("Content-Disposition")
                if (contentDisposition != null && contentDisposition.contains("filename=")) {
                     fileName = contentDisposition.split("filename=")[1].replace("\"", "")
                } else {
                     fileName = getFileNameFromUrl(config.url)
                }
            }

            notifyStart(fileSize)
            
            val file = File(config.saveDir, fileName!!) // fileName is surely not null here
            
            // Setup File
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            val raf = RandomAccessFile(file, "rw")
            raf.setLength(fileSize)
            raf.close()
            
            // Calculate chunks
            val threads = ArrayList<ThreadInfo>()
            val partSize = fileSize / config.threadNum
            for (i in 0 until config.threadNum) {
                val start = i * partSize
                val end = if (i == config.threadNum - 1) fileSize - 1 else (i + 1) * partSize - 1
                threads.add(ThreadInfo(i, start, end, 0))
            }
            
            downloadInfo = DownloadInfo(config.url, fileName!!, fileSize, 0, threads)
            lastProgressTime = System.currentTimeMillis()
            lastProgressBytes = 0
            
            startMultiThreadDownload(file, threads)
            
        } else {
            notifyFail("Connection failed: ${conn.responseCode}")
        }
        conn.disconnect()
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
                    conn.connectTimeout = 5000
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Range", "bytes=$startPos-${info.end}")
                    
                    if (conn.responseCode == HttpURLConnection.HTTP_PARTIAL || conn.responseCode == HttpURLConnection.HTTP_OK) {
                        raf = RandomAccessFile(file, "rw")
                        raf.seek(startPos)
                        
                        input = conn.inputStream
                        val buffer = ByteArray(4096)
                        var len = -1
                        
                        while (input.read(buffer).also { len = it } != -1) {
                            if (isPaused || isCanceled) break
                            
                            raf.write(buffer, 0, len)
                            info.current += len
                            
                            synchronized(this) {
                                downloadInfo?.currentLength = downloadInfo?.currentLength?.plus(len) ?: 0
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastProgressTime >= 1000 || downloadInfo!!.currentLength == downloadInfo!!.totalLength) {
                                    val currentBytes = downloadInfo!!.currentLength
                                    val timeDiff = currentTime - lastProgressTime
                                    val bytesDiff = currentBytes - lastProgressBytes
                                    val speed = if (timeDiff > 0) bytesDiff * 1000 / timeDiff else 0
                                    
                                    val progress = if (downloadInfo!!.totalLength > 0) (downloadInfo!!.currentLength * 100 / downloadInfo!!.totalLength).toInt() else 0
                                    
                                    notifyProgress(progress, currentBytes, speed)
                                    
                                    lastProgressTime = currentTime
                                    lastProgressBytes = currentBytes
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
