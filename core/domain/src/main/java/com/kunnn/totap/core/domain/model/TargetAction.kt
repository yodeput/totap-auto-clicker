package com.kunnn.totap.core.domain.model

/**
 * What a single fire of a target does (spec §4.3).
 *
 * A plain tap (instantaneous touch), a hold at the point, or a drag/swipe
 * gesture from the target point to [Swipe.endX]/[Swipe.endY].
 */
sealed interface TargetAction {
    /** Instantaneous tap at the target point. */
    data object Tap : TargetAction

    /** Press and hold at the target point for [durationMs] milliseconds. */
    data class LongPress(val durationMs: Long) : TargetAction

    /** Drag from the target point to (endX, endY) over [durationMs] milliseconds. */
    data class Swipe(val endX: Float, val endY: Float, val durationMs: Long) : TargetAction
}
