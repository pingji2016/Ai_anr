package com.example.net.download

object DownloadManager {
    private val downloaders = HashMap<String, Downloader>()

    fun download(config: DownloadConfig, listener: DownloadListener) {
        if (downloaders.containsKey(config.url)) {
            // Already exists, maybe resume or ignore
            val downloader = downloaders[config.url]
            downloader?.start()
            return
        }
        
        val downloader = Downloader(config, listener)
        downloaders[config.url] = downloader
        downloader.start()
    }

    fun pause(url: String) {
        downloaders[url]?.pause()
    }

    fun cancel(url: String) {
        downloaders[url]?.cancel()
        downloaders.remove(url)
    }
    
    fun getDownloader(url: String): Downloader? {
        return downloaders[url]
    }
}
