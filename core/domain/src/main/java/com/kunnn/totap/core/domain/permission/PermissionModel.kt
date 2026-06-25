package com.kunnn.totap.core.domain.permission

/**
 * The three system permissions the engine needs before it can run (spec §4.5).
 *
 * - [OVERLAY]:        draw the floating control panel + targets over other apps.
 * - [ACCESSIBILITY]:  the AccessibilityService that dispatches the actual gestures.
 * - [BATTERY]:        exemption from battery optimization so the foreground service survives.
 */
enum class PermissionType { OVERLAY, ACCESSIBILITY, BATTERY }

/** State of a single permission, as observed from the system. */
enum class PermissionState { UNKNOWN, GRANTED, DENIED }

/** A permission and its current observed state. */
data class PermissionStatus(val type: PermissionType, val state: PermissionState) {
    val isGranted: Boolean get() = state == PermissionState.GRANTED
}
