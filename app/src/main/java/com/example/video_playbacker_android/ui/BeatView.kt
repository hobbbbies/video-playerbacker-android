package com.example.video_playbacker_android.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.video_playbacker_android.databinding.ViewBeatBinding

private const val TAG = "BeatView"
class BeatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // get binding somehow
    private val binding = ViewBeatBinding.inflate (
        LayoutInflater.from(context),
        this
    )

    var isEnabledOption: Boolean = false
        set(value) {
            Log.i(TAG, ": setting enabled to $value")
            field = value
            render()
        }

    init {
        render()
    }

    private fun render() {
        val text = if (isEnabledOption) "1" else "0"
        binding.beatIcon.text = text
    }
}