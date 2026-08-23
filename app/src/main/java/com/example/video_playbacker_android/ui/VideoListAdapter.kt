package com.example.video_playbacker_android.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.video_playbacker_android.R
import com.example.video_playbacker_android.network.VideoItem
import kotlinx.coroutines.*
import java.io.IOException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private suspend fun imgUrlToBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        Log.i(TAG, "imgUrlToBitmap: Src: $imageUrl")
        val urlConnection = URL(imageUrl)
        val connection = urlConnection.openConnection()
        connection.connect()
        val input = connection.getInputStream()
        val imgBitmap = BitmapFactory.decodeStream(input)
        Log.i(TAG, "imgUrlToBitmap: Bitmap returned")
        return@withContext imgBitmap
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e(TAG, "Error: ${e.message}")
        return@withContext null
    }
}
private const val TAG = "VideoListAdapter"
class VideoListAdapter(
    private val onClick: (video: VideoItem) -> Unit
) : ListAdapter<VideoItem, VideoListAdapter.ViewHolder>(VideoDiffCallback) {
    private val cachedThumbnails = ConcurrentHashMap<String, Bitmap>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVideoName: TextView = view.findViewById(R.id.tvVideoName)
        val videoThumbnail: ImageView =view.findViewById(R.id.thumbnail)
        var imageLoadJob: Job? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = getItem(position)
        holder.imageLoadJob?.cancel()
        holder.tvVideoName.text = video.snippet.title

        val imageUrl = video.snippet.thumbnails.medium?.url
        if (imageUrl != null) {
             if (cachedThumbnails[imageUrl] != null) {
                 holder.videoThumbnail.setImageBitmap(cachedThumbnails[imageUrl])
             } else {
                 holder.imageLoadJob = CoroutineScope(Dispatchers.Main).launch {
                     val imageBitmap = imgUrlToBitmap(imageUrl) ?: return@launch
                     cachedThumbnails[imageUrl] = imageBitmap
                     holder.videoThumbnail.setImageBitmap(imageBitmap)
                 }
             }
        }
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