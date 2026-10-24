package com.example.rotator.overlay

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
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
 *
 * Supports modern WindowMetrics on Android 11+ (API 30+) and optional magnetic edge snapping.
 */
class DragTouchListener(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val preferencesManager: PreferencesManager,
    private val enableEdgeSnapping: Boolean = false,
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

                if (!isDragging && (abs(dx) > 10 || abs(dy) > 10)) {
                    isDragging = true
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

                if (isDragging) {
                    val (maxX, maxY) = getScreenBounds(v.context, v.width, v.height)
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
                    val (maxX, _) = getScreenBounds(v.context, v.width, v.height)

                    if (enableEdgeSnapping && maxX > 0) {
                        // Snap horizontally to nearest edge (left 0 or right maxX)
                        layoutParams.x = if (layoutParams.x < maxX / 2) 0 else maxX
                        try {
                            windowManager.updateViewLayout(v, layoutParams)
                        } catch (ignored: Exception) {
                        }
                    }

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

    private fun getScreenBounds(context: Context, viewWidth: Int, viewHeight: Int): Pair<Int, Int> {
        val (screenWidth, screenHeight) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            Pair(metrics.bounds.width(), metrics.bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val displayMetrics = context.resources.displayMetrics
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
        val maxX = max(0, screenWidth - viewWidth)
        val maxY = max(0, screenHeight - viewHeight)
        return Pair(maxX, maxY)
    }
}

