package com.example.net

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.net.download.DownloadConfig
import com.example.net.download.DownloadListener
import com.example.net.download.DownloadManager
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.net.test", appContext.packageName)
    }

    @Test
    fun testDownloadHtml() {
        // 1. 获取Context和保存路径
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val saveDir = appContext.getExternalFilesDir(null)?.absolutePath 
            ?: appContext.filesDir.absolutePath
            
        // 2. 配置下载任务：默认地址 https://zhaofm.xyz/
        val url = "https://zhaofm.xyz/"
        val config = DownloadConfig(
            url = url,
            saveDir = saveDir,
            fileName = "zhaofm_index.html", // 指定文件名
            threadNum = 3
        )
        
        println("开始下载测试: $url -> $saveDir")
        
        val latch = CountDownLatch(1)
        var isSuccess = false
        
        // 3. 监听回调
        val listener = object : DownloadListener {
            override fun onStart(url: String, totalLength: Long) {
                println("onStart: 开始下载, 总大小: $totalLength")
            }

            override fun onProgress(url: String, progress: Int, currentLength: Long, speed: Long) {
                // 打印进度
                println("onProgress: $progress%, $currentLength bytes, Speed: $speed B/s")
            }

            override fun onSuccess(url: String, path: String) {
                println("onSuccess: 下载成功, 文件路径: $path")
                isSuccess = true
                latch.countDown()
            }

            override fun onPause(url: String) {
                println("onPause: 下载暂停")
            }

            override fun onFail(url: String, msg: String) {
                println("onFail: 下载失败 - $msg")
                latch.countDown()
            }
        }
        DownloadManager.download(config, listener)
        
        // 4. 等待结果
        latch.await(30, TimeUnit.SECONDS)
        assertTrue("下载应该成功", isSuccess)
    }
}