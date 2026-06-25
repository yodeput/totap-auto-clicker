package com.kunnn.totap.core.domain.model

/**
 * Immutable snapshot of what the engine should execute for one run (spec §4.2).
 *
 * Built from a [ClickConfig] at run time. The planner reads this to produce
 * the gesture sequence. A session never mutates; a new run builds a new session.
 */
data class ClickSession(
    val config: ClickConfig,
    val maxTotalRepeats: Int = 0,
)
