package com.example.video_playbacker_android.player

import android.R.attr.delay
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.video_playbacker_android.ui.BeatCircleView
import com.example.video_playbacker_android.ui.BeatView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "BeatManager"
class BeatManager(private val bpm: Float, private val beatFrames: List<Float>, private val timeSig: Int = 4) {
    private val interval = 60 / bpm
    private var currBeat = 1
    private var beatJob: Job? = null

    init {
        require(timeSig > 0)
        require(beatFrames.zipWithNext().all { (a, b) -> a <= b }) {
            "Beat frames must be sorted"
        }
    }

    fun beatIndexAt(second: Float): Int? {
        val result = beatFrames.binarySearch(second)

        val frameIndex = if (result >= 0) {
            result
        } else {
            -result - 2 // not found
        }

        if (frameIndex < 0) return null

        return (frameIndex % timeSig) + 1
    }

    suspend fun delayWithFloat(floatDelay: Float) {
        val milliseconds = (floatDelay * 1000).toLong()
        delay(milliseconds)
    }

    fun pause() {
        beatJob?.cancel()
    }
}