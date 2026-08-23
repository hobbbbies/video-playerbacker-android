package com.example.video_playbacker_android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_playbacker_android.network.VideoItem
import com.example.video_playbacker_android.network.VideoSearchSnippet
import com.example.video_playbacker_android.network.YoutubeDataAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

sealed interface YoutubeDataUiState {
    data class Success(val videos: List<VideoItem>) : YoutubeDataUiState
    data class Error(val errorMsg: String) : YoutubeDataUiState
    object Loading : YoutubeDataUiState
}

private const val TAG = "Viewmodel"
class PlayerViewModel(): ViewModel() {
    private val _searchUiState = MutableStateFlow<YoutubeDataUiState>(YoutubeDataUiState.Loading)
    val searchUiState = _searchUiState.asStateFlow()

    private val _chosenVideo = MutableStateFlow<VideoItem?>(null)
    val chosenVideo = _chosenVideo.asStateFlow()

    fun getVideosBySearch(query: String) {
        viewModelScope.launch {
            try {
                val key = BuildConfig.YOUTUBE_DATA_API_KEY
                val searchResult = YoutubeDataAPI.retrofitService.search(key, query, maxResults = 5)
                _searchUiState.value = YoutubeDataUiState.Success(searchResult.items)
            } catch(e: IOException) {
                _searchUiState.value = YoutubeDataUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun setChosenVideo(video: VideoItem) {
        _chosenVideo.value = video
    }
}