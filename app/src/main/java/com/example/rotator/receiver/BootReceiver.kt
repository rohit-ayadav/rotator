package com.example.rotator.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.rotator.core.PreferencesManager
import com.example.rotator.service.RotationService
import com.example.rotator.util.PermissionManager

/**
 * BroadcastReceiver restoring the screen rotation controller when the device completes booting
 * or when the package is updated, if enabled in user preferences.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val prefs = PreferencesManager(context)
        val shouldRestore = prefs.startOnBoot || prefs.wasRunningBeforeShutdown

        if (shouldRestore && PermissionManager.hasOverlayPermission(context)) {
            Log.d(TAG, "Restoring Rotator service after device boot / package update")
            try {
                RotationService.startService(context, prefs.lastOrientationMode)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Rotator service on boot", e)
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
