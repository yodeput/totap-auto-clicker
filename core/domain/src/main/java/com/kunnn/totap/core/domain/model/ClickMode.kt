package com.kunnn.totap.core.domain.model

/**
 * How multiple targets are ordered in time (spec §4.3).
 *
 * - [SINGLE]: only targets[0] fires, repeatedly on its interval.
 * - [MULTI]:  targets fire round-robin; before each target, wait that target's interval.
 * - [SYNC]:   all targets fire at the same instant; the cycle advances by the
 *             longest interval among them; repeat.
 */
enum class ClickMode { SINGLE, MULTI, SYNC }
