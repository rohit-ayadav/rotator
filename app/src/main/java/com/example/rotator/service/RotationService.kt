package com.example.rotator.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.rotator.core.ControllerState
import com.example.rotator.core.OrientationEnforcer
import com.example.rotator.core.OrientationMode
import com.example.rotator.core.PreferencesManager
import com.example.rotator.core.ServiceState
import com.example.rotator.overlay.OverlayController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service orchestrating screen orientation enforcement,
 * floating overlay lifecycle, and persistent notification updates.
 */
class RotationService : Service(), OverlayController.Callbacks {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var orientationEnforcer: OrientationEnforcer
    private lateinit var overlayController: OverlayController

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        notificationHelper = NotificationHelper(this)
        orientationEnforcer = OrientationEnforcer(this)
        overlayController = OverlayController(this, preferencesManager, this)

        val initialMode = preferencesManager.lastOrientationMode
        val isHidden = preferencesManager.isPanelHidden
        val isCompact = preferencesManager.isCompactMode

        _controllerState.value = ControllerState(
            serviceState = ServiceState.RUNNING,
            orientationMode = initialMode,
            isPanelHidden = isHidden,
            isCompactMode = isCompact
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        // Start foreground immediately to satisfy Android background execution requirements
        startAsForegroundService()

        when (action) {
            ACTION_START -> {
                val modeName = intent?.getStringExtra(EXTRA_MODE_NAME)
                val mode = OrientationMode.fromName(modeName, preferencesManager.lastOrientationMode)
                startController(mode)
            }

            ACTION_SET_MODE -> {
                val modeName = intent?.getStringExtra(EXTRA_MODE_NAME)
                val mode = OrientationMode.fromName(modeName, _controllerState.value.orientationMode)
                setOrientationMode(mode)
            }

            ACTION_PAUSE -> {
                pauseController()
            }

            ACTION_RESUME -> {
                resumeController()
            }

            ACTION_HIDE_PANEL -> {
                hideFloatingPanel()
            }

            ACTION_SHOW_PANEL -> {
                showFloatingPanel()
            }

            ACTION_TOGGLE_PANEL -> {
                if (_controllerState.value.isPanelHidden) {
                    showFloatingPanel()
                } else {
                    hideFloatingPanel()
                }
            }

            ACTION_STOP -> {
                stopController()
                return START_NOT_STICKY
            }
        }

        return START_STICKY
    }

    private fun startAsForegroundService() {
        val notification = notificationHelper.buildNotification(_controllerState.value)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }

    private fun startController(mode: OrientationMode) {
        preferencesManager.lastOrientationMode = mode
        orientationEnforcer.applyOrientation(mode)

        updateState {
            it.copy(
                serviceState = ServiceState.RUNNING,
                orientationMode = mode
            )
        }

        if (!_controllerState.value.isPanelHidden && canDrawOverlays()) {
            overlayController.show(_controllerState.value)
        }
        updateNotification()
    }

    private fun setOrientationMode(mode: OrientationMode) {
        preferencesManager.lastOrientationMode = mode
        if (_controllerState.value.serviceState == ServiceState.RUNNING) {
            orientationEnforcer.applyOrientation(mode)
        }

        updateState {
            it.copy(orientationMode = mode)
        }

        overlayController.update(_controllerState.value)
        updateNotification()
    }

    private fun pauseController() {
        orientationEnforcer.pause()
        updateState {
            it.copy(serviceState = ServiceState.PAUSED)
        }

        overlayController.update(_controllerState.value)
        updateNotification()
    }

    private fun resumeController() {
        val currentMode = _controllerState.value.orientationMode
        orientationEnforcer.resume(currentMode)

        updateState {
            it.copy(serviceState = ServiceState.RUNNING)
        }

        overlayController.update(_controllerState.value)
        updateNotification()
    }

    private fun hideFloatingPanel() {
        preferencesManager.isPanelHidden = true
        overlayController.hide()

        updateState {
            it.copy(isPanelHidden = true)
        }
        updateNotification()
    }

    private fun showFloatingPanel() {
        if (!canDrawOverlays()) return

        preferencesManager.isPanelHidden = false
        updateState {
            it.copy(isPanelHidden = false)
        }

        overlayController.show(_controllerState.value)
        updateNotification()
    }

    private fun stopController() {
        orientationEnforcer.stop()
        overlayController.destroy()

        updateState {
            it.copy(serviceState = ServiceState.STOPPED)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        try {
            val notification = notificationHelper.buildNotification(_controllerState.value)
            NotificationManagerCompat.from(this).notify(NotificationHelper.NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted for update: ${e.message}")
        }
    }

    private fun updateState(reducer: (ControllerState) -> ControllerState) {
        _controllerState.value = reducer(_controllerState.value)
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    // OverlayController.Callbacks
    override fun onOrientationSelected(mode: OrientationMode) {
        setOrientationMode(mode)
    }

    override fun onPauseResumeToggle() {
        if (_controllerState.value.isPaused) {
            resumeController()
        } else {
            pauseController()
        }
    }

    override fun onHidePanel() {
        hideFloatingPanel()
    }

    override fun onStopRequested() {
        stopController()
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationEnforcer.stop()
        overlayController.destroy()
        _controllerState.value = ControllerState(serviceState = ServiceState.STOPPED)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "RotationService"

        const val ACTION_START = "com.example.rotator.action.START"
        const val ACTION_STOP = "com.example.rotator.action.STOP"
        const val ACTION_PAUSE = "com.example.rotator.action.PAUSE"
        const val ACTION_RESUME = "com.example.rotator.action.RESUME"
        const val ACTION_SET_MODE = "com.example.rotator.action.SET_MODE"
        const val ACTION_HIDE_PANEL = "com.example.rotator.action.HIDE_PANEL"
        const val ACTION_SHOW_PANEL = "com.example.rotator.action.SHOW_PANEL"
        const val ACTION_TOGGLE_PANEL = "com.example.rotator.action.TOGGLE_PANEL"

        const val EXTRA_MODE_NAME = "extra_mode_name"

        private val _controllerState = MutableStateFlow(ControllerState())
        val controllerState: StateFlow<ControllerState> = _controllerState.asStateFlow()

        fun startService(context: Context, mode: OrientationMode? = null) {
            val intent = Intent(context, RotationService::class.java).apply {
                action = ACTION_START
                if (mode != null) {
                    putExtra(EXTRA_MODE_NAME, mode.name)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun sendAction(context: Context, actionName: String, mode: OrientationMode? = null) {
            val intent = Intent(context, RotationService::class.java).apply {
                action = actionName
                if (mode != null) {
                    putExtra(EXTRA_MODE_NAME, mode.name)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
