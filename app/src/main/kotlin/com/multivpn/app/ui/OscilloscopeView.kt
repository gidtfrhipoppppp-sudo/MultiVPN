package com.multivpn.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class OscilloscopeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            phase += 0.16f
            invalidate()
            handler.postDelayed(this, 16)
        }
    }

    private var phase = 0f
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4FC3F7")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80DEEA")
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        alpha = 70
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(updateRunnable)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f
        val gradient = LinearGradient(
            0f, 0f, 0f, height,
            intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#111827")),
            null,
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = gradient
        canvas.drawRect(0f, 0f, width, height, fillPaint)

        for (i in 0..8) {
            val y = (height / 8f) * i
            canvas.drawLine(0f, y, width, y, gridPaint)
        }

        val samples = 220
        val step = width / samples
        val path = Path()
        val glowPath = Path()
        var first = true

        for (i in 0..samples) {
            val x = i * step
            val normalized = i.toFloat() / samples.toFloat()
            val wave = (sin((normalized * 8f + phase).toDouble()) * 0.45 + cos((normalized * 6f - phase * 0.8f).toDouble()) * 0.25)
            val amplitude = (height * 0.18f) + (abs(sin((normalized * 3f + phase * 0.5f).toDouble())) * height * 0.12f)
            val y = centerY + (wave * amplitude).toFloat()
            if (first) {
                path.moveTo(x, y)
                glowPath.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
                glowPath.lineTo(x, y)
            }
        }

        glowPaint.setShadowLayer(10f, 0f, 0f, Color.parseColor("#4DD0E1"))
        canvas.drawPath(glowPath, glowPaint)
        canvas.drawPath(path, wavePaint)

        val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22D3EE")
            style = Paint.Style.FILL
        }
        val pulseX = width * 0.85f
        val pulseY = centerY + (sin(phase.toDouble()) * height * 0.12f).toFloat()
        canvas.drawCircle(pulseX, pulseY, 8f, pulsePaint)
    }
}
