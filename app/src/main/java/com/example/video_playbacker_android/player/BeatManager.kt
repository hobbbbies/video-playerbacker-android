package com.example.video_playbacker_android.player

import android.R.attr.delay
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.video_playbacker_android.ui.BeatView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "BeatManager"
class BeatManager(private val coroutineScope: CoroutineScope, private val bpm: Float, private val beatFrames: List<Int>, private val beatViews: List<BeatView>, private val timeSig: Int = 4) {
    private val interval = 60 / bpm
    private var currBeat = 1

    init {
        if (bpm < 1) {
            throw IllegalArgumentException("BPM cannot be below zero")
        }

        coroutineScope.launch {
            while(true) {
                playBeat()
                delayWithFloat(interval)
                Log.i(TAG, ": beat!")
            }
        }
    }

    suspend fun playBeat() = withContext(Dispatchers.Main) {
        Log.i(TAG, "playBeat: $currBeat beats enabled")
        for (i in 0..<currBeat) {
            beatViews[i].isEnabledOption = true
        }
        Log.i(TAG, "playBeat: ${timeSig-currBeat} beats disabled")
        for (i in currBeat..<timeSig) {
            beatViews[i].isEnabledOption = false
        }
        currBeat = (currBeat % timeSig) + 1
    }

    suspend fun delayWithFloat(floatDelay: Float) {
        val milliseconds = (floatDelay * 1000).toLong()
        delay(milliseconds)
    }
}