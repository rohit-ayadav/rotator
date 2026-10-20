package com.example.rotator.core

/**
 * Lifecycle states of the rotation controller service.
 */
enum class ServiceState {
    STOPPED,
    RUNNING,
    PAUSED
}

/**
 * Immutable snapshot of the global controller state.
 */
data class ControllerState(
    val serviceState: ServiceState = ServiceState.STOPPED,
    val orientationMode: OrientationMode = OrientationMode.PORTRAIT,
    val isPanelHidden: Boolean = false,
    val isCompactMode: Boolean = false
) {
    val isRunning: Boolean get() = serviceState == ServiceState.RUNNING
    val isPaused: Boolean get() = serviceState == ServiceState.PAUSED
    val isStopped: Boolean get() = serviceState == ServiceState.STOPPED
    val isEnforcing: Boolean get() = isRunning
}
