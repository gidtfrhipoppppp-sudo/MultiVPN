package com.multivpn.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.util.LinkedList
import kotlin.math.abs

class OscilloscopeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val samples = LinkedList<Float>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5EEAD4")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        style = Paint.Style.FILL
    }

    fun pushSample(value: Float) {
        synchronized(samples) {
            samples.addLast(value)
            while (samples.size > 200) {
                samples.removeFirst()
            }
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        for (i in 0 until 6) {
            val y = (height / 6f) * i
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        val path = Path()
        synchronized(samples) {
            if (samples.isEmpty()) {
                return
            }
            val step = width.toFloat() / maxOf(1, samples.size - 1)
            for ((index, sample) in samples.withIndex()) {
                val x = index * step
                val normalized = ((sample % 2f) + 2f) / 4f
                val y = height * (1f - normalized)
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
        }
        canvas.drawPath(path, paint)
    }
}
