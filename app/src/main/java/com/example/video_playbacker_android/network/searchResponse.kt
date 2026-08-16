package com.example.video_playbacker_android.network

import kotlinx.serialization.Serializable

@Serializable
data class VideoSearchResponse(
    val kind: String,
    val etag: String,
    val nextPageToken: String? = null,
    val regionCode: String? = null,
    val pageInfo: PageInfo,
    val items: List<VideoItem>
)

@Serializable
data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

@Serializable
data class VideoItem(
    val kind: String,
    val etag: String,
    val id: VideoId,
    val snippet: VideoSearchSnippet
)

@Serializable
data class VideoId(
    val kind: String,
    val videoId: String? = null,
    val channelId: String? = null,
    val playlistId: String? = null
)

@Serializable
data class VideoSearchSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String,
    val liveBroadcastContent: String,
    val publishTime: String
)

@Serializable
data class Thumbnails(
    val default: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val high: Thumbnail? = null
)

@Serializable
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)
