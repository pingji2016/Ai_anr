package com.example.tfdemo.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfdemo.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.widget.ImageButton

class LocalFileAdapter(private val onDeleteClick: (File) -> Unit) : RecyclerView.Adapter<LocalFileAdapter.FileViewHolder>() {

    private val files = mutableListOf<File>()

    fun setFiles(newFiles: List<File>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_file, parent, false)
        return FileViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    class FileViewHolder(itemView: View, private val onDeleteClick: (File) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: TextView = itemView.findViewById(R.id.tv_file_name)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tv_file_size)
        private val tvFileDate: TextView = itemView.findViewById(R.id.tv_file_date)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_file)

        fun bind(file: File) {
            tvFileName.text = file.name
            tvFileSize.text = formatSize(file.length())
            tvFileDate.text = formatDate(file.lastModified())
            
            btnDelete.setOnClickListener {
                onDeleteClick(file)
            }
        }

        private fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}