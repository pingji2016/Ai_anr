package com.example.tfdemo.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfdemo.R

data class DownloadTaskUiState(
    val url: String,
    var fileName: String = "Unknown",
    var progress: Int = 0,
    var status: String = "Idle",
    var currentSize: Long = 0,
    var totalSize: Long = 0,
    var speed: Long = 0
)

class DownloadAdapter : RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {

    private val tasks = mutableListOf<DownloadTaskUiState>()

    fun addTask(task: DownloadTaskUiState) {
        tasks.add(0, task)
        notifyItemInserted(0)
    }

    fun updateTask(url: String, progress: Int? = null, status: String? = null, fileName: String? = null, currentSize: Long? = null, totalSize: Long? = null, speed: Long? = null) {
        val index = tasks.indexOfFirst { it.url == url }
        if (index != -1) {
            val task = tasks[index]
            if (progress != null) task.progress = progress
            if (status != null) task.status = status
            if (fileName != null) task.fileName = fileName
            if (currentSize != null) task.currentSize = currentSize
            if (totalSize != null) task.totalSize = totalSize
            if (speed != null) task.speed = speed
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvFileName: TextView = view.findViewById(R.id.tv_file_name)
        private val tvUrl: TextView = view.findViewById(R.id.tv_url)
        private val pbProgress: ProgressBar = view.findViewById(R.id.pb_progress)
        private val tvPercent: TextView = view.findViewById(R.id.tv_percent)
        private val tvStatus: TextView = view.findViewById(R.id.tv_status)
        private val tvSpeed: TextView = view.findViewById(R.id.tv_speed)
        private val tvSize: TextView = view.findViewById(R.id.tv_size)

        fun bind(task: DownloadTaskUiState) {
            tvFileName.text = task.fileName
            tvUrl.text = task.url
            pbProgress.progress = task.progress
            tvPercent.text = "${task.progress}%"
            tvStatus.text = task.status
            tvSpeed.text = formatSpeed(task.speed)
            tvSize.text = "${formatSize(task.currentSize)} / ${formatSize(task.totalSize)}"
        }

        private fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }

        private fun formatSpeed(bytesPerSec: Long): String {
             return "${formatSize(bytesPerSec)}/s"
        }
    }
}
