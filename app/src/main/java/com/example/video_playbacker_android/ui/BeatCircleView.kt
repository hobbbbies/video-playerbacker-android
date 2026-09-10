package com.example.video_playbacker_android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

private const val TAG = "BeatView"
class BeatCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isEnabledOption: Boolean = false
        set(value) {
            Log.i(TAG, ": setting enabled to $value")
            field = value
            circlePaint.style = if (value == true) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
            invalidate()
        }
    private var diameter = 0f

    private val circlePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = 0x0
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Account for padding.
        var xPad = (paddingLeft + paddingRight).toFloat()
        val yPad = (paddingTop + paddingBottom).toFloat()

        val ww = w.toFloat() - xPad
        val hh = h.toFloat() - yPad

        // Figure out how big you can make the circle.
        diameter = Math.min(ww, hh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            val radius = diameter / 2
            drawCircle(radius, radius, radius, circlePaint)
        }
    }

//    private fun render() {
//        val text = if (isEnabledOption) "1" else "0"
//        binding.beatIcon.text = text
//    }
}