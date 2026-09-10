package com.example.video_playbacker_android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_playbacker_android.network.VideoItem
import com.example.video_playbacker_android.network.VideoSearchSnippet
import com.example.video_playbacker_android.network.YoutubeDataApi
import com.example.video_playbacker_android.network.PythonApi
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

sealed interface BeatsDataUiState {
    data class Success(val bpm: Float, val beatFrames: List<Int>) : BeatsDataUiState
    data class Error(val errorMsg: String) : BeatsDataUiState
    object Loading : BeatsDataUiState
}

private const val TAG = "Viewmodel"
class PlayerViewModel(): ViewModel() {
    private val _searchUiState = MutableStateFlow<YoutubeDataUiState>(YoutubeDataUiState.Loading)
    val searchUiState = _searchUiState.asStateFlow()
    private val _beatsUiState = MutableStateFlow<BeatsDataUiState>(BeatsDataUiState.Loading)
    val beatsUiState = _beatsUiState.asStateFlow()

    private val _chosenVideo = MutableStateFlow<VideoItem?>(null)
    val chosenVideo = _chosenVideo.asStateFlow()

    fun getVideosBySearch(query: String) {
        viewModelScope.launch {
            try {
                val key = BuildConfig.YOUTUBE_DATA_API_KEY
                val searchResult = YoutubeDataApi.retrofitService.search(key, query, maxResults = 5)
                _searchUiState.value = YoutubeDataUiState.Success(searchResult.items)
            } catch(e: Exception) {
                _searchUiState.value = YoutubeDataUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun setChosenVideo(video: VideoItem) {
        _chosenVideo.value = video
        val videoId = video.id.videoId
        if (videoId != null) getBeats(videoId)
    }

    fun getBeats(videoId: String) {
        Log.i(TAG, "getBeats: getting beats...")
        viewModelScope.launch {
            try {
                val result = PythonApi.retrofitService.beats(videoId)
                _beatsUiState.value = BeatsDataUiState.Success(result.bpm, result.beatFrames)
            } catch(e: Exception) {
                _beatsUiState.value = BeatsDataUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }
}