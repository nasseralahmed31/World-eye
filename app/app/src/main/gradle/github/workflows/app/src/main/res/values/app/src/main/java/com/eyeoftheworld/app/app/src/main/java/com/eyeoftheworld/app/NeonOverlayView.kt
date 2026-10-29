
package com.eyeoftheworld.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class NeonOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(0, 229, 255)
        style = Paint.Style.STROKE
        strokeWidth = 12f
        alpha = 100
        setShadowLayer(16f, 0f, 0f, Color.CYAN)
    }

    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(190, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        textSize = 36f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private var objects: List<TrackedDetection> = emptyList()
    private var sourceWidth = 1
    private var sourceHeight = 1

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun submit(
        detections: List<TrackedDetection>,
        frameWidth: Int,
        frameHeight: Int
    ) {
        objects = detections
        sourceWidth = frameWidth.coerceAtLeast(1)
        sourceHeight = frameHeight.coerceAtLeast(1)
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sx = width.toFloat() / sourceWidth
        val sy = height.toFloat() / sourceHeight

        canvas.save()
        canvas.scale(sx, sy)

        for (obj in objects) {
            val rect = RectF(obj.bounds)

            canvas.drawRoundRect(rect, 12f, 12f, glowPaint)
            canvas.drawRoundRect(rect, 12f, 12f, edgePaint)

            val name = LabelUtils.getLabelAr(obj.labelIndex)
            val caption = "$name #${obj.id}"

            canvas.drawText(
                caption,
                rect.left.coerceAtLeast(4f),
                (rect.top - 10f).coerceAtLeast(38f),
                labelPaint
            )
        }

        canvas.restore()
    }

    fun clear() {
        objects = emptyList()
        postInvalidateOnAnimation()
    }
}
