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
class BeatManager(private val coroutineScope: CoroutineScope, private val bpm: Float, private val beatFrames: List<Float>, private val timeSig: Int = 4, private val beatIndex: MutableStateFlow<Int>) {
    private val interval = 60 / bpm
    private var currBeat = 1
    private var beatJob: Job? = null

    init {
        if (bpm < 1) {
            throw IllegalArgumentException("BPM cannot be below zero")
        }

        beatJob = coroutineScope.launch {
            while(true) {
                playBeat()
                delayWithFloat(interval)
                Log.i(TAG, ": beat!")
            }
        }
    }

    suspend fun playBeat() = withContext(Dispatchers.Main) {
        Log.i(TAG, "playBeat: $currBeat beats enabled")
        currBeat = (currBeat % timeSig) + 1
        beatIndex.value = currBeat
    }

    suspend fun delayWithFloat(floatDelay: Float) {
        val milliseconds = (floatDelay * 1000).toLong()
        delay(milliseconds)
    }

    fun pause() {
        beatJob?.cancel()
    }
}