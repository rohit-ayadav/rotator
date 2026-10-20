package com.example.rotator.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages lightweight persistent preferences such as overlay coordinates and last used mode.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastOrientationMode: OrientationMode
        get() {
            val name = prefs.getString(KEY_ORIENTATION_MODE, OrientationMode.PORTRAIT.name)
            return OrientationMode.fromName(name)
        }
        set(value) {
            prefs.edit().putString(KEY_ORIENTATION_MODE, value.name).apply()
        }

    var overlayX: Int
        get() = prefs.getInt(KEY_OVERLAY_X, 100)
        set(value) {
            prefs.edit().putInt(KEY_OVERLAY_X, value).apply()
        }

    var overlayY: Int
        get() = prefs.getInt(KEY_OVERLAY_Y, 200)
        set(value) {
            prefs.edit().putInt(KEY_OVERLAY_Y, value).apply()
        }

    var isCompactMode: Boolean
        get() = prefs.getBoolean(KEY_COMPACT_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_COMPACT_MODE, value).apply()
        }

    var isPanelHidden: Boolean
        get() = prefs.getBoolean(KEY_PANEL_HIDDEN, false)
        set(value) {
            prefs.edit().putBoolean(KEY_PANEL_HIDDEN, value).apply()
        }

    var startOnBoot: Boolean
        get() = prefs.getBoolean(KEY_START_ON_BOOT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_START_ON_BOOT, value).apply()
        }

    var wasRunningBeforeShutdown: Boolean
        get() = prefs.getBoolean(KEY_WAS_RUNNING, false)
        set(value) {
            prefs.edit().putBoolean(KEY_WAS_RUNNING, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "rotator_preferences"
        private const val KEY_ORIENTATION_MODE = "key_orientation_mode"
        private const val KEY_OVERLAY_X = "key_overlay_x"
        private const val KEY_OVERLAY_Y = "key_overlay_y"
        private const val KEY_COMPACT_MODE = "key_compact_mode"
        private const val KEY_PANEL_HIDDEN = "key_panel_hidden"
        private const val KEY_START_ON_BOOT = "key_start_on_boot"
        private const val KEY_WAS_RUNNING = "key_was_running"
    }
}
