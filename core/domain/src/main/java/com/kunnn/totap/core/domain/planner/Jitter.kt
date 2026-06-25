package com.kunnn.totap.core.domain.planner

import com.kunnn.totap.core.domain.model.JitterConfig
import kotlin.random.Random

/**
 * Pure anti-detection jitter math (spec §4.4).
 *
 * Each fire can be nudged within a symmetric range so no two taps land
 * identically. Functions are pure: they take the source of randomness
 * explicitly so tests are reproducible with a seeded [Random].
 */
object Jitter {

    /**
     * Returns jittered (x, y) for a base coordinate.
     *
     * With [jitter].positionJitterPx = n, each axis independently gets an
     * offset uniformly sampled from [-n, +n] as a Float (sub-pixel precision).
     * n = 0 returns the base coordinates unchanged.
     */
    fun applyPositionJitter(
        baseX: Float,
        baseY: Float,
        jitter: JitterConfig,
        random: Random,
    ): Pair<Float, Float> {
        if (jitter.positionJitterPx <= 0) return baseX to baseY
        val max = jitter.positionJitterPx.toFloat()
        // nextFloat() is in [0,1); map to [-1,1) then scale by max.
        val dx = (random.nextFloat() * 2f - 1f) * max
        val dy = (random.nextFloat() * 2f - 1f) * max
        return (baseX + dx) to (baseY + dy)
    }

    /**
     * Returns a jittered fire time in milliseconds.
     *
     * With [jitter].timeJitterMs = m, the base time gets an offset uniformly
     * sampled from [-m, +m] (Long). The result is clamped to >= 0 so a tiny
     * base time never produces a negative (pre-start) fire instant.
     */
    fun applyTimeJitter(
        baseMs: Long,
        jitter: JitterConfig,
        random: Random,
    ): Long {
        if (jitter.timeJitterMs <= 0L) return baseMs
        val range = jitter.timeJitterMs
        // random.nextLong(bound) is [0, bound); center on zero.
        val offset = random.nextLong(2 * range + 1) - range
        val result = baseMs + offset
        return if (result < 0L) 0L else result
    }
}
