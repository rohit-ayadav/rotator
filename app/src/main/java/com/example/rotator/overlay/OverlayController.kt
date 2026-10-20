package com.example.rotator.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.rotator.R
import com.example.rotator.core.ControllerState
import com.example.rotator.core.OrientationMode
import com.example.rotator.core.PreferencesManager
import com.example.rotator.core.ServiceState
import com.example.rotator.databinding.OverlayPanelBinding
import com.example.rotator.databinding.OverlayPanelCompactBinding

/**
 * Controller for the movable, interactive floating UI panel overlay.
 */
class OverlayController(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val callbacks: Callbacks
) {

    interface Callbacks {
        fun onOrientationSelected(mode: OrientationMode)
        fun onPauseResumeToggle()
        fun onHidePanel()
        fun onStopRequested()
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var currentView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isCompact: Boolean = preferencesManager.isCompactMode
    private var lastState: ControllerState = ControllerState()
    var isPanelVisible: Boolean = false
        private set

    /**
     * Displays the floating panel if not already visible.
     */
    fun show(state: ControllerState) {
        lastState = state
        if (isPanelVisible && currentView != null) {
            update(state)
            return
        }
        createAndAttachOverlay(isCompact)
        update(state)
    }

    /**
     * Hides the floating panel without affecting orientation enforcement.
     */
    fun hide() {
        removeCurrentOverlay()
        isPanelVisible = false
    }

    /**
     * Updates the UI elements based on the current controller state.
     */
    fun update(state: ControllerState) {
        lastState = state
        if (!isPanelVisible || currentView == null) return

        if (isCompact) {
            updateCompactView(state)
        } else {
            updateExpandedView(state)
        }
    }

    /**
     * Removes all views and cleans up resources.
     */
    fun destroy() {
        removeCurrentOverlay()
        isPanelVisible = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createAndAttachOverlay(compact: Boolean) {
        removeCurrentOverlay()

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = preferencesManager.overlayX
            y = preferencesManager.overlayY
        }

        try {
            val themedContext = androidx.appcompat.view.ContextThemeWrapper(context, R.style.Theme_Rotator)
            val inflater = LayoutInflater.from(themedContext)
            val view = if (compact) {
                setupCompactView(inflater, params)
            } else {
                setupExpandedView(inflater, params)
            }

            windowManager.addView(view, params)
            currentView = view
            layoutParams = params
            isPanelVisible = true
            isCompact = compact
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add floating overlay window", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupExpandedView(
        inflater: LayoutInflater,
        params: WindowManager.LayoutParams
    ): View {
        val binding = OverlayPanelBinding.inflate(inflater)

        // Setup drag listener on header
        val dragListener = DragTouchListener(windowManager, params, preferencesManager)
        binding.dragBar.setOnTouchListener(dragListener)

        // Orientation buttons
        binding.btnOverlayPortrait.setOnClickListener {
            callbacks.onOrientationSelected(OrientationMode.PORTRAIT)
        }
        binding.btnOverlayReversePortrait.setOnClickListener {
            callbacks.onOrientationSelected(OrientationMode.REVERSE_PORTRAIT)
        }
        binding.btnOverlayLandscapeLeft.setOnClickListener {
            callbacks.onOrientationSelected(OrientationMode.LANDSCAPE_LEFT)
        }
        binding.btnOverlayLandscapeRight.setOnClickListener {
            callbacks.onOrientationSelected(OrientationMode.LANDSCAPE_RIGHT)
        }
        binding.btnOverlayAutoRotate.setOnClickListener {
            callbacks.onOrientationSelected(OrientationMode.AUTO_ROTATE)
        }

        // Actions
        binding.btnOverlayPauseResume.setOnClickListener {
            callbacks.onPauseResumeToggle()
        }
        binding.btnOverlayCompact.setOnClickListener {
            isCompact = true
            preferencesManager.isCompactMode = true
            createAndAttachOverlay(compact = true)
            update(lastState)
        }
        binding.btnOverlayHide.setOnClickListener {
            callbacks.onHidePanel()
        }
        binding.btnOverlayStop.setOnClickListener {
            callbacks.onStopRequested()
        }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCompactView(
        inflater: LayoutInflater,
        params: WindowManager.LayoutParams
    ): View {
        val binding = OverlayPanelCompactBinding.inflate(inflater)

        // Entire compact pill is draggable via drag handle or background
        val dragListener = DragTouchListener(windowManager, params, preferencesManager)
        binding.ivCompactDragHandle.setOnTouchListener(dragListener)
        binding.compactRoot.setOnTouchListener(dragListener)

        binding.btnCompactPauseResume.setOnClickListener {
            callbacks.onPauseResumeToggle()
        }
        binding.btnCompactExpand.setOnClickListener {
            isCompact = false
            preferencesManager.isCompactMode = false
            createAndAttachOverlay(compact = false)
            update(lastState)
        }

        return binding.root
    }

    private fun updateExpandedView(state: ControllerState) {
        val view = currentView ?: return
        try {
            val binding = OverlayPanelBinding.bind(view)

            // Update active orientation highlight
            val activeBg = ContextCompat.getDrawable(context, R.drawable.bg_button_circle_active)
            val defaultBg = ContextCompat.getDrawable(context, R.drawable.bg_button_circle)

            binding.btnOverlayPortrait.background =
                if (state.orientationMode == OrientationMode.PORTRAIT) activeBg else defaultBg
            binding.btnOverlayReversePortrait.background =
                if (state.orientationMode == OrientationMode.REVERSE_PORTRAIT) activeBg else defaultBg
            binding.btnOverlayLandscapeLeft.background =
                if (state.orientationMode == OrientationMode.LANDSCAPE_LEFT) activeBg else defaultBg
            binding.btnOverlayLandscapeRight.background =
                if (state.orientationMode == OrientationMode.LANDSCAPE_RIGHT) activeBg else defaultBg
            binding.btnOverlayAutoRotate.background =
                if (state.orientationMode == OrientationMode.AUTO_ROTATE) activeBg else defaultBg

            // Update Pause / Resume button
            if (state.isPaused) {
                binding.btnOverlayPauseResume.text = context.getString(R.string.action_resume)
                binding.btnOverlayPauseResume.setIconResource(R.drawable.ic_play)
                binding.tvOverlayTitle.text = context.getString(R.string.status_paused)
            } else {
                binding.btnOverlayPauseResume.text = context.getString(R.string.action_pause)
                binding.btnOverlayPauseResume.setIconResource(R.drawable.ic_pause)
                binding.tvOverlayTitle.text = context.getString(R.string.app_name)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating expanded view: ${e.message}")
        }
    }

    private fun updateCompactView(state: ControllerState) {
        val view = currentView ?: return
        try {
            val binding = OverlayPanelCompactBinding.bind(view)

            binding.ivCompactCurrentMode.setImageResource(state.orientationMode.iconRes)

            if (state.isPaused) {
                binding.btnCompactPauseResume.setImageResource(R.drawable.ic_play)
            } else {
                binding.btnCompactPauseResume.setImageResource(R.drawable.ic_pause)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating compact view: ${e.message}")
        }
    }

    private fun removeCurrentOverlay() {
        val view = currentView
        if (view != null) {
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay view: ${e.message}")
            } finally {
                currentView = null
                layoutParams = null
            }
        }
    }

    companion object {
        private const val TAG = "OverlayController"
    }
}
