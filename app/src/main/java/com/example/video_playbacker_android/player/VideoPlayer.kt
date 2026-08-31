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
import kotlinx.coroutines.launch

private const val TAG = "VideoPlayer"
class VideoPlayer(private val youtubePlayer: YouTubePlayer, private val viewModel: PlayerViewModel, private val scope: LifecycleCoroutineScope, lifecycleOwner: LifecycleOwner) {
    var currentSecond: Float = 0f
    var playerState: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNKNOWN

    private var recordingLoop = false
    private var loopStart: Float? = null
    private var loopEnd: Float? = null

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
            if (loopEnd!! - loopStart!! > 1.0) {
                Log.i(TAG, "handleLoop: ending loop at: $loopEnd")
                recordingLoop = false
            }
        }

        checkLoop() // should seek instantly
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
}