package com.example.rotator.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.rotator.MainActivity
import com.example.rotator.R
import com.example.rotator.core.OrientationMode
import com.example.rotator.core.ServiceState
import com.example.rotator.util.PermissionManager

/**
 * Quick Settings Tile service allowing users to view current orientation
 * and cycle through modes directly from the notification shade.
 */
@RequiresApi(Build.VERSION_CODES.N)
class RotatorTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        val state = RotationService.controllerState.value

        if (state.serviceState == ServiceState.STOPPED) {
            if (PermissionManager.hasOverlayPermission(this)) {
                RotationService.startService(this)
            } else {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            }
        } else {
            // Cycle to the next orientation mode
            val nextMode = getNextMode(state.orientationMode)
            RotationService.sendAction(this, RotationService.ACTION_SET_MODE, nextMode)
            if (state.isPaused) {
                RotationService.sendAction(this, RotationService.ACTION_RESUME)
            }
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val state = RotationService.controllerState.value

        when (state.serviceState) {
            ServiceState.RUNNING -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.app_name)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(state.orientationMode.titleRes)
                }
                tile.icon = Icon.createWithResource(this, state.orientationMode.iconRes)
            }

            ServiceState.PAUSED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.app_name)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.status_paused)
                }
                tile.icon = Icon.createWithResource(this, state.orientationMode.iconRes)
            }

            ServiceState.STOPPED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.app_name)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.status_stopped)
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_rotator_logo)
            }
        }

        tile.updateTile()
    }

    private fun getNextMode(current: OrientationMode): OrientationMode {
        return when (current) {
            OrientationMode.PORTRAIT -> OrientationMode.LANDSCAPE_LEFT
            OrientationMode.LANDSCAPE_LEFT -> OrientationMode.REVERSE_PORTRAIT
            OrientationMode.REVERSE_PORTRAIT -> OrientationMode.LANDSCAPE_RIGHT
            OrientationMode.LANDSCAPE_RIGHT -> OrientationMode.AUTO_ROTATE
            OrientationMode.AUTO_ROTATE -> OrientationMode.PORTRAIT
        }
    }

    companion object {
        fun updateTile(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestListeningState(
                        context,
                        ComponentName(context, RotatorTileService::class.java)
                    )
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
