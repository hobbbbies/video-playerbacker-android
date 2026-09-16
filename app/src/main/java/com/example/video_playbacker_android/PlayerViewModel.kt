package com.example.video_playbacker_android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_playbacker_android.network.VideoItem
import com.example.video_playbacker_android.network.YoutubeDataApi
import com.example.video_playbacker_android.network.PythonApi
import com.example.video_playbacker_android.player.BeatManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface YoutubeDataUiState {
    data class Success(val videos: List<VideoItem>) : YoutubeDataUiState
    data class Error(val errorMsg: String) : YoutubeDataUiState
    object Loading : YoutubeDataUiState
}

sealed interface BeatsDataUiState {
    data class Success(val bpm: Float, val beatFrames: List<Float>) : BeatsDataUiState
    data class Error(val errorMsg: String) : BeatsDataUiState
    object Loading : BeatsDataUiState
}

data class PlayerUiState(
    val currentSecond: Float = 0f,
    val duration: Float = 0f,
    val playerState: PlayerState = PlayerState.UNKNOWN,
    val loopStart: Float? = null,
    val loopEnd: Float? = null,
    val isRecordingLoop: Boolean = false,
)

sealed interface PlayerEffect {
    data class SeekTo(val second: Float) : PlayerEffect
}

private const val TAG = "Viewmodel"
class PlayerViewModel(): ViewModel() {
    private val _searchUiState = MutableStateFlow<YoutubeDataUiState>(YoutubeDataUiState.Loading)
    val searchUiState = _searchUiState.asStateFlow()
    private val _beatsUiState = MutableStateFlow<BeatsDataUiState>(BeatsDataUiState.Loading)
    val beatsUiState = _beatsUiState.asStateFlow()

    private var beatManager: BeatManager? = null

    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState = _playerUiState.asStateFlow()

    private val _playerEffects = MutableSharedFlow<PlayerEffect>(extraBufferCapacity = 1)
    val playerEffects = _playerEffects.asSharedFlow()

    private val _beatIndex = MutableStateFlow<Int>(0)
    val beatIndex = _beatIndex.asStateFlow()
    val timeSig = 4

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

    fun onCurrentSecondChanged(second: Float) {
        val state = _playerUiState.value.copy(currentSecond = second)
        _playerUiState.value = state

        val loopStart = state.loopStart
        val loopEnd = state.loopEnd
        if (loopStart != null && loopEnd != null &&
            (second !in loopStart..<loopEnd)
        ) {
            _playerEffects.tryEmit(PlayerEffect.SeekTo(loopStart))
        }
        _beatIndex.value = beatManager?.beatIndexAt(second) ?: _beatIndex.value
    }

    // .copy is pattern for data class flows
    fun onPlayerStateChanged(state: PlayerState) {
        _playerUiState.value = _playerUiState.value.copy(playerState = state)
    }

    fun onVideoDurationChanged(duration: Float) {
        _playerUiState.value = _playerUiState.value.copy(duration = duration)
    }

    fun toggleLoop() {
        val state = _playerUiState.value
        _playerUiState.value = if (!state.isRecordingLoop) {
            state.copy(
                loopStart = state.currentSecond,
                loopEnd = null,
                isRecordingLoop = true,
            )
        } else {
            val loopStart = state.loopStart
            if (loopStart != null && state.currentSecond - loopStart > 1f) {
                state.copy(
                    loopEnd = state.currentSecond,
                    isRecordingLoop = false,
                )
            } else {
                state
            }
        }
    }

    fun clearLoop() {
        _playerUiState.value = _playerUiState.value.copy(
            loopStart = null,
            loopEnd = null,
            isRecordingLoop = false,
        )
    }

    fun getBeats(videoId: String) {
        Log.i(TAG, "getBeats: getting beats...")
        _beatsUiState.value = BeatsDataUiState.Loading
        viewModelScope.launch {
            try {
                val result = PythonApi.retrofitService.beats(videoId)
                _beatsUiState.value = BeatsDataUiState.Success(result.bpm, result.beatFrames)
            } catch(e: Exception) {
                _beatsUiState.value = BeatsDataUiState.Error(e.message ?: "An error occurred.")
            }
        }
    }

    fun setBeatManager(bpm: Float, beatFrames: List<Float>) {
        beatManager = BeatManager(bpm, beatFrames, timeSig)
    }

    fun clearBeatManager() {
        beatManager = null
    }
}
