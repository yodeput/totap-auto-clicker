package com.kunnn.totap.core.domain.permission

/**
 * Reads the current state of the system permissions the engine depends on.
 *
 * The Android implementation queries `Settings.canDrawOverlays()`, the
 * AccessibilityManager, and PowerManager. This interface keeps that detail out
 * of features and makes the "all granted?" predicate unit-testable.
 */
interface PermissionChecker {

    /** The current state of all three permissions, in the canonical order. */
    fun status(): List<PermissionStatus>

    /** Convenience: the overlay permission status. */
    fun overlay(): PermissionStatus = status().first { it.type == PermissionType.OVERLAY }

    /** Convenience: the accessibility permission status. */
    fun accessibility(): PermissionStatus = status().first { it.type == PermissionType.ACCESSIBILITY }

    /** Convenience: the battery-optimization exemption status. */
    fun battery(): PermissionStatus = status().first { it.type == PermissionType.BATTERY }

    companion object {
        /**
         * Pure: true iff every permission in [statuses] is granted.
         * Used by both the real checker and tests; also used to gate `start()`.
         */
        fun canStart(statuses: List<PermissionStatus>): Boolean =
            PermissionType.entries.all { type ->
                statuses.any { it.type == type && it.isGranted }
            }
    }
}
