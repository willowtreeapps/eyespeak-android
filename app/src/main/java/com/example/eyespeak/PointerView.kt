package com.example.eyespeak

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PointerView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint: Paint = Paint().apply { color = 0xFFFFFFFF.toInt() }
    private val radius: Float = 100f

    private var xAdjusted: Float = 0f
    private var yAdjusted: Float = 0f

    private var xPercent: Int = 0
    private var yPercent: Int = 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(xAdjusted, yAdjusted, radius, paint)
    }

    fun updatePointerPositionPercent(xPercent: Int, yPercent: Int) {
        this.xPercent = xPercent
        this.yPercent = yPercent
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calcAdjustedCoords(w, h)
    }

    private fun calcAdjustedCoords(width: Int, height: Int) {
        xAdjusted = (width / 100F * xPercent)
        yAdjusted = (height / 100F * yPercent)
    }
}
