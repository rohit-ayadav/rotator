package com.example.rotator.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ControllerStateTest {

    @Test
    fun testDefaultState() {
        val state = ControllerState()
        assertEquals(ServiceState.STOPPED, state.serviceState)
        assertEquals(OrientationMode.PORTRAIT, state.orientationMode)
        assertFalse(state.isPanelHidden)
        assertFalse(state.isCompactMode)
        assertTrue(state.isStopped)
        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isEnforcing)
    }

    @Test
    fun testRunningState() {
        val state = ControllerState(
            serviceState = ServiceState.RUNNING,
            orientationMode = OrientationMode.LANDSCAPE_LEFT,
            isPanelHidden = true,
            isCompactMode = true
        )
        assertFalse(state.isStopped)
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertTrue(state.isEnforcing)
        assertTrue(state.isPanelHidden)
        assertTrue(state.isCompactMode)
    }

    @Test
    fun testPausedState() {
        val state = ControllerState(
            serviceState = ServiceState.PAUSED,
            orientationMode = OrientationMode.AUTO_ROTATE
        )
        assertFalse(state.isStopped)
        assertFalse(state.isRunning)
        assertTrue(state.isPaused)
        assertFalse(state.isEnforcing)
    }

    @Test
    fun testStateCopyMutation() {
        val initial = ControllerState(serviceState = ServiceState.RUNNING)
        val paused = initial.copy(serviceState = ServiceState.PAUSED)
        assertTrue(paused.isPaused)
        assertFalse(paused.isRunning)

        val modeChanged = paused.copy(orientationMode = OrientationMode.REVERSE_PORTRAIT)
        assertEquals(OrientationMode.REVERSE_PORTRAIT, modeChanged.orientationMode)
        assertTrue(modeChanged.isPaused)
    }
}
