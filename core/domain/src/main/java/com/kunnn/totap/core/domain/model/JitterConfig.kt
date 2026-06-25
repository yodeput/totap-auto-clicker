package com.kunnn.totap.core.domain.model

/**
 * Anti-detection jitter configuration (spec §4.4).
 *
 * Applied per fire: random offset within [-max, +max] is added to the target's
 * coordinates and/or the scheduled fire time, so no two taps land identically.
 * Both values are zero by default (jitter off); the user opts in per-target or
 * globally.
 *
 * @property positionJitterPx max pixels of random offset applied independently to x and y.
 * @property timeJitterMs     max milliseconds of random offset applied to the fire time.
 */
data class JitterConfig(
    val positionJitterPx: Int = 0,
    val timeJitterMs: Long = 0L,
)
