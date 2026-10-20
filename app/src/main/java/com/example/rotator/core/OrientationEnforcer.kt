package com.example.rotator.core

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Invisible overlay window that applies and maintains the requested screen orientation
 * across the entire device using WindowManager.LayoutParams.screenOrientation.
 *
 * Separating this from the interactive floating UI ensures orientation enforcement
 * persists even when the floating panel is hidden or minimized.
 */
class OrientationEnforcer(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var enforcerView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isAttached = false

    /**
     * Applies the requested orientation. Attaches the invisible overlay if not already attached.
     */
    fun applyOrientation(mode: OrientationMode) {
        val targetOrientation = mode.activityInfoOrientation
        ensureAttached(targetOrientation)
        updateOrientation(targetOrientation)
    }

    /**
     * Suspends orientation enforcement by resetting the overlay window's orientation request
     * to UNSPECIFIED, allowing normal system rotation behavior.
     */
    fun pause() {
        updateOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    /**
     * Resumes enforcement of the provided orientation mode.
     */
    fun resume(mode: OrientationMode) {
        applyOrientation(mode)
    }

    /**
     * Removes the enforcer view completely from the WindowManager.
     */
    fun stop() {
        if (isAttached && enforcerView != null) {
            try {
                windowManager.removeView(enforcerView)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing orientation enforcer view: ${e.message}")
            } finally {
                enforcerView = null
                layoutParams = null
                isAttached = false
            }
        }
    }

    private fun ensureAttached(initialOrientation: Int) {
        if (isAttached && enforcerView != null) return

        val view = View(context)
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            0,
            0,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            screenOrientation = initialOrientation
        }

        try {
            windowManager.addView(view, params)
            enforcerView = view
            layoutParams = params
            isAttached = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach orientation enforcer view", e)
        }
    }

    private fun updateOrientation(orientation: Int) {
        val view = enforcerView
        val params = layoutParams
        if (isAttached && view != null && params != null) {
            if (params.screenOrientation != orientation) {
                params.screenOrientation = orientation
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update orientation enforcer layout", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "OrientationEnforcer"
    }
}
