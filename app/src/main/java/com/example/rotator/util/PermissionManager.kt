package com.example.rotator.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utility helper for managing overlay and notification permissions across Android versions.
 */
object PermissionManager {

    /**
     * Checks if the application has permission to draw overlays (SYSTEM_ALERT_WINDOW).
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Opens the system settings screen for granting overlay permission with safe fallback.
     */
    fun requestOverlayPermission(activity: Activity, requestCode: Int = 1001) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
                activity.startActivityForResult(intent, requestCode)
            } catch (e: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    activity.startActivityForResult(fallbackIntent, requestCode)
                } catch (e2: Exception) {
                    android.widget.Toast.makeText(
                        activity,
                        "Please open device Settings > Apps > Rotator and grant 'Display over other apps'",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Checks if the app has notification permission (required on Android 13+ / API 33+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
