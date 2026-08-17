package com.example.video_playbacker_android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.video_playbacker_android.R
import com.example.video_playbacker_android.network.VideoItem

class VideoListAdapter(
    private val onClick: (video: VideoItem) -> Unit
) : ListAdapter<VideoItem, VideoListAdapter.ViewHolder>(VideoDiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVideoName: TextView = view.findViewById(R.id.tvVideoName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = getItem(position)
        holder.tvVideoName.text = video.snippet.title
        holder.itemView.setOnClickListener { onClick(video) }
    }

    companion object VideoDiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }
}