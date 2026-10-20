package com.example.rotator.overlay

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.rotator.core.PreferencesManager
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * OnTouchListener that allows dragging a floating WindowManager overlay view
 * while keeping it clamped within usable screen bounds.
 */
class DragTouchListener(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val preferencesManager: PreferencesManager,
    private val onDragEnd: ((x: Int, y: Int) -> Unit)? = null
) : View.OnTouchListener {

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isDragging: Boolean = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - initialTouchX).toInt()
                val dy = (event.rawY - initialTouchY).toInt()

                if (abs(dx) > 10 || abs(dy) > 10) {
                    isDragging = true
                }

                if (isDragging) {
                    val displayMetrics = v.resources.displayMetrics
                    val maxX = max(0, displayMetrics.widthPixels - v.width)
                    val maxY = max(0, displayMetrics.heightPixels - v.height)

                    layoutParams.x = min(max(0, initialX + dx), maxX)
                    layoutParams.y = min(max(0, initialY + dy), maxY)

                    try {
                        windowManager.updateViewLayout(v, layoutParams)
                    } catch (ignored: Exception) {
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    preferencesManager.overlayX = layoutParams.x
                    preferencesManager.overlayY = layoutParams.y
                    onDragEnd?.invoke(layoutParams.x, layoutParams.y)
                } else {
                    v.performClick()
                }
                return true
            }
        }
        return false
    }
}
