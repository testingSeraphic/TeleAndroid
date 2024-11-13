package com.tele.android.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.FrameLayout

class RoundedCornerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        val radius = 16f // Adjust corner radius
        val path = Path().apply {
            addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, Path.Direction.CW)
        }
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}