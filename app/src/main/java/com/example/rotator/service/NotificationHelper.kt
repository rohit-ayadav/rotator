package com.example.rotator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.rotator.MainActivity
import com.example.rotator.R
import com.example.rotator.core.ControllerState
import com.example.rotator.core.OrientationMode

/**
 * Builds and updates the persistent ongoing notification with rotation controls.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(state: ControllerState): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (state.isPaused) {
            context.getString(R.string.notif_title_paused)
        } else {
            context.getString(R.string.notif_title_running)
        }

        val modeName = context.getString(state.orientationMode.titleRes)
        val contentText = context.getString(R.string.notif_content_format, modeName)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(state.orientationMode.iconRes)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Action 1: Pause / Resume Toggle
        val pauseResumeAction = if (state.isPaused) {
            createServiceAction(
                RotationService.ACTION_RESUME,
                context.getString(R.string.action_resume),
                R.drawable.ic_play,
                REQUEST_PAUSE_RESUME
            )
        } else {
            createServiceAction(
                RotationService.ACTION_PAUSE,
                context.getString(R.string.action_pause),
                R.drawable.ic_pause,
                REQUEST_PAUSE_RESUME
            )
        }
        builder.addAction(pauseResumeAction)

        // Action 2: Quick Auto-Rotate Action
        val autoRotateIntent = Intent(context, RotationService::class.java).apply {
            action = RotationService.ACTION_SET_MODE
            putExtra(RotationService.EXTRA_MODE_NAME, OrientationMode.AUTO_ROTATE.name)
        }
        val autoRotatePendingIntent = PendingIntent.getService(
            context,
            REQUEST_AUTO_ROTATE,
            autoRotateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_auto_rotate,
            context.getString(R.string.mode_auto_rotate),
            autoRotatePendingIntent
        )

        // Action 3: Toggle Floating Panel
        val panelToggleTitle = if (state.isPanelHidden) {
            context.getString(R.string.action_show_panel)
        } else {
            context.getString(R.string.action_hide_panel)
        }
        val panelToggleIcon = if (state.isPanelHidden) {
            R.drawable.ic_visibility
        } else {
            R.drawable.ic_visibility_off
        }
        builder.addAction(
            createServiceAction(
                RotationService.ACTION_TOGGLE_PANEL,
                panelToggleTitle,
                panelToggleIcon,
                REQUEST_TOGGLE_PANEL
            )
        )

        // Action 4: Stop Controller
        builder.addAction(
            createServiceAction(
                RotationService.ACTION_STOP,
                context.getString(R.string.action_stop),
                R.drawable.ic_stop,
                REQUEST_STOP
            )
        )

        return builder.build()
    }

    private fun createServiceAction(
        actionName: String,
        title: String,
        iconRes: Int,
        requestCode: Int
    ): NotificationCompat.Action {
        val intent = Intent(context, RotationService::class.java).apply {
            action = actionName
        }
        val pendingIntent = PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(iconRes, title, pendingIntent).build()
    }

    companion object {
        const val CHANNEL_ID = "rotator_controller_channel"
        const val NOTIFICATION_ID = 1001

        private const val REQUEST_OPEN_APP = 101
        private const val REQUEST_PAUSE_RESUME = 102
        private const val REQUEST_AUTO_ROTATE = 103
        private const val REQUEST_TOGGLE_PANEL = 104
        private const val REQUEST_STOP = 105
    }
}
