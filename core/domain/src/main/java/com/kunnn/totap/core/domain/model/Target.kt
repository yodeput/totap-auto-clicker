package com.kunnn.totap.core.domain.model

/**
 * One tap/hold/swipe point in a config (spec §4.2).
 *
 * @property id           stable identifier (for UI editing & persistence).
 * @property x, y         screen coordinates in pixels.
 * @property action       what a single fire does.
 * @property intervalMs   delay in ms before THIS target fires (see [ClickMode]).
 * @property repeat       max fires for this target; 0 = unlimited.
 * @property jitter       per-target anti-detection jitter (overrides global if non-zero).
 */
data class Target(
    val id: String,
    val x: Float,
    val y: Float,
    val action: TargetAction = TargetAction.Tap,
    val intervalMs: Long = 100L,
    val repeat: Int = 0,
    val jitter: JitterConfig = JitterConfig(),
)
