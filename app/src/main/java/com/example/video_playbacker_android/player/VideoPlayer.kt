package com.example.video_playbacker_android.player

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.video_playbacker_android.PlayerViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import kotlinx.coroutines.launch


private const val TAG = "VideoPlayer"
class VideoPlayer(private val youtubePlayer: YouTubePlayer, private val viewModel: PlayerViewModel, private val scope: LifecycleCoroutineScope, private val lifecycleOwner: LifecycleOwner) {
    var currentSecond: Float = 0f
        internal set
    var playerState: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNKNOWN
        internal set
    var videoDuration: Float = 0f
        internal set
    var loopStart: Float? = null
        private set
    var loopEnd: Float? = null
        private set
    private var recordingLoop = false

    init {
        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chosenVideo.collect { video ->
                    video?.id?.videoId?.let { id ->
                        youtubePlayer.loadVideo(id, 0f)
                    }
                }
            }
        }
    }

    fun seekTo(timestamp: Float) {
        youtubePlayer.seekTo(timestamp)
    }

    fun pauseOrPlay() {
        if (playerState == PlayerState.PLAYING) youtubePlayer.pause() else youtubePlayer.play()
    }

    fun handleLoop(): Boolean {
        if (!recordingLoop) {
            loopStart = currentSecond
            loopEnd = null
            Log.i(TAG, "handleLoop: Starting loop at: $loopStart")
            recordingLoop = true
        } else {
            loopEnd = currentSecond
            if (loopStart === null) {
                throw Error("loopStart was null when trying to end loop")
            }
            if (currentSecond - loopStart!! > 1.0) {
                loopEnd = currentSecond
                Log.i(TAG, "handleLoop: ending loop at: $loopEnd")
                recordingLoop = false
            }
        }

        return recordingLoop // represents if a loop was started or not
    }

    fun checkLoop() {
        if (loopStart == null || loopEnd == null) {
            return
        }

        if (currentSecond >= loopEnd!! || currentSecond < loopStart!!) {
            Log.i(TAG, "checkLoop: LOOPING")
            seekTo(loopStart!!)
        }
    }

    fun clearLoop() {
        recordingLoop = false
        loopStart = null
        loopEnd = null
    }

    fun addBeatManager() {

    }
}