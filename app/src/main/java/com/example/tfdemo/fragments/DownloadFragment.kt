package com.example.tfdemo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.net.download.DownloadConfig
import com.example.net.download.DownloadListener
import com.example.net.download.DownloadManager
import com.example.tfdemo.R
import java.io.File

import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView

class DownloadFragment : Fragment() {

    private lateinit var etUrl: EditText
    private lateinit var btnDownload: Button
    private lateinit var rvDownloads: RecyclerView
    private lateinit var spinnerUrl: Spinner
    private lateinit var adapter: DownloadAdapter

    private val presetUrls = listOf(
        "https://zhaofm.xyz/",
        "https://microdown.myapp.com/ug/%E5%BA%94%E7%94%A8%E5%AE%9D.apk?comment=channelId%3D1181533%26context%3D%26scheme%3Dtmast%253A%252F%252Fencrypt%253Fencryptdata%253Dff1HUq1nz4010Rd9420%25252FY0khCJOpaRaYWLCr59T9mXlfVEw6kVKGGb9ztEeSViKykz06gLlGM7L%25252Bf0YyULDyOC3xZ90k9RELGBRllpNFzL1ehrpaA%25252FN3NxkZrNI3aUADo%25252F6fiXdIvk%25252BmZnxZ2wc5ZSPBBsXkwwLezuIh01rMnByWGrD5YhKvQLPWfsd4lMUHqeVZPGbtA984hXXpvmGGWM41SKJxb1DOxAnesQrvPo%25252FE0sC3L68RABejocTVfoZUdDNaRjCbST3tIDjutnxCAhBQBD3CgMuSsNY8n4qLBIqjs3C4g1RFyEUlK1SogfzEtmKOa1hYX%25252FjW7ycRjvL%25252BIoVAGION%25252BXH8TppakJNgoCp0F159jAnx74DDf6pvXdlWHiYF6vAYvU68jLa5zigw7T6HSrlPME4izfpN1Ii1SUwtAnd7zCvYrzzA6iq7mkzVF2hLKSqvagxs51aCe0bGm09dBLpYLvsLca8mBxHMRctCl1poUeukRFF3tzuFEz3ERMJxMCUSJd370d4ZMrJZcEAL8fQTSrLRq9QZBxz6jnINcnpFAiBuwx%25252F4ToEQQ66CfOpWOKIvVkep9%25252BdBkqqxZzojDz8dIAcR2MBFbjgvYbYU11ObYGBweam0oS23VAOrhwiusfs0Q1uVUtu0D%25252FpI%25252BJlQrcb10lIC%25252B08m%25252FR6pz5tAQ6K%25252F7QRo6hJs1lcVTdkNXlplszxMGtRd4jzH503fJ23GXnOl8I3W27JDrl9qYXKdL1xv%25252Fn4gojl%25252FgplSdW87icNc3YlSzy1nvrz1ARoPzfjt%25252BI3DcCXNjTTtPR4XAvD0FTca1gG5Yg%25253D%25253D%2526start_time%253D1766677192942%2526expiry_time%253D1766680792942%26t%3D1766677192000%26signature%3DaARS9%252BoBYv3Fv4KKt%252FPyOCnqVsOvtk%252FFjunDfWoqC9Pf9Jq1ZDwVj8JB3cjXOhSCMYUrved%252B%252Fr1%252BGzshkco18drrLyOlSOqUUMLqTu9bCntgx8Uo179NJpxxPiYIX%252BwsL10Fhtvhy7wwwxiZcMM%252FKWFQAuC%252BUXJEZUELXg7E9s0%253D&sign_type=V2&realname=20251225_263647728e0f59848a6eb368dbb3b958_offset_31006720",
        "https://down.pc.yyb.qq.com/pcyyb/packing/64eab25b5880264f20076f55f5ba3fb4/2957d433c0232ede2939dd02e5ac29b47a381e03.exe",
        "https://lf9-apk.ugapk.cn/package/apk/aweme/5072_370201/aweme_update_zlink_v5072_370201_9107_1766515162.apk?v=1766515175",
        "https://dldir1v6.qq.com/qqfile/qq/QQNT/Windows/QQ_9.9.25_251203_x64_01.exe"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUrl = view.findViewById(R.id.et_url)
        btnDownload = view.findViewById(R.id.btn_download)
        rvDownloads = view.findViewById(R.id.rv_downloads)
        spinnerUrl = view.findViewById(R.id.spinner_url)

        setupRecyclerView()
        setupSpinner()

        btnDownload.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                startDownload(url)
                etUrl.text.clear()
            } else {
                Toast.makeText(requireContext(), "Please enter a URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, presetUrls)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUrl.adapter = adapter
        
        spinnerUrl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etUrl.setText(presetUrls[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = DownloadAdapter()
        rvDownloads.layoutManager = LinearLayoutManager(requireContext())
        rvDownloads.adapter = adapter
    }

    private fun startDownload(url: String) {
        // Create a task UI item
        val taskUi = DownloadTaskUiState(url = url, status = "Pending")
        adapter.addTask(taskUi)

        val saveDir = requireContext().getExternalFilesDir(null)?.absolutePath 
            ?: requireContext().filesDir.absolutePath

        val config = DownloadConfig(
            url = url,
            saveDir = saveDir,
            fileName = null // Let it infer
        )

        DownloadManager.download(config, object : DownloadListener {
            override fun onStart(url: String, totalLength: Long) {
                activity?.runOnUiThread {
                    adapter.updateTask(url, status = "Started", totalSize = totalLength)
                }
            }

            override fun onProgress(url: String, progress: Int, currentLength: Long, speed: Long) {
                activity?.runOnUiThread {
                    adapter.updateTask(url, progress = progress, status = "Downloading", currentSize = currentLength, speed = speed)
                }
            }

            override fun onSuccess(url: String, path: String) {
                activity?.runOnUiThread {
                    val fileName = File(path).name
                    adapter.updateTask(url, progress = 100, status = "Completed", fileName = fileName, speed = 0)
                    Toast.makeText(requireContext(), "Download Complete: $fileName", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPause(url: String) {
                activity?.runOnUiThread {
                    adapter.updateTask(url, status = "Paused", speed = 0)
                }
            }

            override fun onFail(url: String, msg: String) {
                activity?.runOnUiThread {
                    adapter.updateTask(url, status = "Failed: $msg", speed = 0)
                }
            }
        })
    }
}
