package com.example.rotator.core

import android.content.pm.ActivityInfo
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.rotator.R

/**
 * Supported orientation modes mapping to Android ActivityInfo screen orientation constants.
 */
enum class OrientationMode(
    val activityInfoOrientation: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
) {
    PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        R.drawable.ic_portrait,
        R.string.mode_portrait
    ),
    REVERSE_PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        R.drawable.ic_reverse_portrait,
        R.string.mode_reverse_portrait
    ),
    LANDSCAPE_LEFT(
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        R.drawable.ic_landscape_left,
        R.string.mode_landscape_left
    ),
    LANDSCAPE_RIGHT(
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        R.drawable.ic_landscape_right,
        R.string.mode_landscape_right
    ),
    AUTO_ROTATE(
        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
        R.drawable.ic_auto_rotate,
        R.string.mode_auto_rotate
    );

    fun next(): OrientationMode {
        return when (this) {
            PORTRAIT -> LANDSCAPE_LEFT
            LANDSCAPE_LEFT -> REVERSE_PORTRAIT
            REVERSE_PORTRAIT -> LANDSCAPE_RIGHT
            LANDSCAPE_RIGHT -> AUTO_ROTATE
            AUTO_ROTATE -> PORTRAIT
        }
    }

    companion object {
        fun fromName(name: String?, default: OrientationMode = PORTRAIT): OrientationMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: default
        }
    }
}
