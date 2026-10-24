package com.example.rotator.core

import android.content.pm.ActivityInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationModeTest {

    @Test
    fun testFromNameValid() {
        assertEquals(OrientationMode.PORTRAIT, OrientationMode.fromName("PORTRAIT"))
        assertEquals(OrientationMode.REVERSE_PORTRAIT, OrientationMode.fromName("REVERSE_PORTRAIT"))
        assertEquals(OrientationMode.LANDSCAPE_LEFT, OrientationMode.fromName("LANDSCAPE_LEFT"))
        assertEquals(OrientationMode.LANDSCAPE_RIGHT, OrientationMode.fromName("LANDSCAPE_RIGHT"))
        assertEquals(OrientationMode.AUTO_ROTATE, OrientationMode.fromName("AUTO_ROTATE"))
    }

    @Test
    fun testFromNameInvalidFallsBackToDefault() {
        assertEquals(OrientationMode.PORTRAIT, OrientationMode.fromName("UNKNOWN_MODE"))
        assertEquals(OrientationMode.PORTRAIT, OrientationMode.fromName(null))
        assertEquals(
            OrientationMode.LANDSCAPE_LEFT,
            OrientationMode.fromName("INVALID", default = OrientationMode.LANDSCAPE_LEFT)
        )
    }

    @Test
    fun testActivityInfoConstantsMapping() {
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, OrientationMode.PORTRAIT.activityInfoOrientation)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, OrientationMode.REVERSE_PORTRAIT.activityInfoOrientation)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, OrientationMode.LANDSCAPE_LEFT.activityInfoOrientation)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, OrientationMode.LANDSCAPE_RIGHT.activityInfoOrientation)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, OrientationMode.AUTO_ROTATE.activityInfoOrientation)
    }

    @Test
    fun testNextModeCycling() {
        assertEquals(OrientationMode.LANDSCAPE_LEFT, OrientationMode.PORTRAIT.next())
        assertEquals(OrientationMode.REVERSE_PORTRAIT, OrientationMode.LANDSCAPE_LEFT.next())
        assertEquals(OrientationMode.LANDSCAPE_RIGHT, OrientationMode.REVERSE_PORTRAIT.next())
        assertEquals(OrientationMode.AUTO_ROTATE, OrientationMode.LANDSCAPE_RIGHT.next())
        assertEquals(OrientationMode.PORTRAIT, OrientationMode.AUTO_ROTATE.next())
    }
}
