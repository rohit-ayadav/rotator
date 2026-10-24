package com.example.rotator.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages lightweight persistent preferences such as overlay coordinates and last used mode.
 */
class PreferencesManager(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

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

    var overlayOpacity: Int
        get() = prefs.getInt(KEY_OVERLAY_OPACITY, 90).coerceIn(20, 100)
        set(value) {
            prefs.edit().putInt(KEY_OVERLAY_OPACITY, value.coerceIn(20, 100)).apply()
        }

    val overlayAlpha: Float
        get() = overlayOpacity / 100f

    var autoCollapseIdle: Boolean
        get() = prefs.getBoolean(KEY_AUTO_COLLAPSE_IDLE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_COLLAPSE_IDLE, value).apply()
        }

    var autoCollapseTimeoutSeconds: Int
        get() = prefs.getInt(KEY_AUTO_COLLAPSE_TIMEOUT, 5).coerceIn(3, 30)
        set(value) {
            prefs.edit().putInt(KEY_AUTO_COLLAPSE_TIMEOUT, value.coerceIn(3, 30)).apply()
        }

    var snapToEdge: Boolean
        get() = prefs.getBoolean(KEY_SNAP_TO_EDGE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SNAP_TO_EDGE, value).apply()
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
        private const val KEY_OVERLAY_OPACITY = "key_overlay_opacity"
        private const val KEY_AUTO_COLLAPSE_IDLE = "key_auto_collapse_idle"
        private const val KEY_AUTO_COLLAPSE_TIMEOUT = "key_auto_collapse_timeout"
        private const val KEY_SNAP_TO_EDGE = "key_snap_to_edge"
    }
}
