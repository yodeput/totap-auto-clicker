package com.kunnn.totap.core.domain.model

/**
 * A single scheduled gesture produced by the [GesturePlanner][com.kunnn.totap.core.domain.planner.GesturePlanner].
 *
 * The concrete gesture carries the (already jittered) coordinates and the
 * gesture duration (0 for an instantaneous tap).
 *
 * @property fireAtMs  milliseconds after session start when this gesture should dispatch.
 * @property gesture   the concrete gesture to perform.
 */
data class PlannedGesture(
    val fireAtMs: Long,
    val gesture: Gesture,
)

/**
 * The concrete gesture shape dispatched to the AccessibilityService.
 * Coordinates are already jittered. durationMs = 0 means an instantaneous tap.
 */
sealed interface Gesture {
    val durationMs: Long

    /** Tap (or hold) at (x, y) for [durationMs]. durationMs = 0 → instantaneous tap. */
    data class Tap(val x: Float, val y: Float, override val durationMs: Long = 0L) : Gesture

    /** Drag from (startX, startY) to (endX, endY) over [durationMs]. */
    data class Swipe(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        override val durationMs: Long,
    ) : Gesture
}
