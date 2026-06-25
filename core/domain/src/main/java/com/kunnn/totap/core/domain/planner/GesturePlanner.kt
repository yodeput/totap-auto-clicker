package com.kunnn.totap.core.domain.planner

import com.kunnn.totap.core.domain.model.ClickMode
import com.kunnn.totap.core.domain.model.ClickSession
import com.kunnn.totap.core.domain.model.Gesture
import com.kunnn.totap.core.domain.model.JitterConfig
import com.kunnn.totap.core.domain.model.PlannedGesture
import com.kunnn.totap.core.domain.model.TargetAction
import kotlin.random.Random

/**
 * Turns a [ClickSession] into a lazy, open-ended sequence of [PlannedGesture]s
 * (spec §4). The engine (Phase 3) consumes this sequence and dispatches each
 * gesture to the AccessibilityService at its [PlannedGesture.fireAtMs].
 *
 * Behaviour by mode:
 * - [ClickMode.SINGLE]: only targets[0] fires, on its interval.
 * - [ClickMode.MULTI]:  round-robin over targets; before each target wait its interval.
 * - [ClickMode.SYNC]:   all targets fire at the same instant each cycle; the cycle
 *                       advances by the longest target interval.
 *
 * Anti-detection jitter (spec §4.4): when [ClickSession.antiDetection] (via config)
 * is enabled, each fire's coordinates and time are jittered via [Jitter].
 *
 * The planner is deterministic given a seeded [random], which makes it fully
 * unit-testable without a device.
 *
 * @param random source of randomness for jitter. Inject for reproducible tests.
 */
class GesturePlanner(private val random: Random = Random.Default) {

    fun plan(session: ClickSession): Sequence<PlannedGesture> = sequence {
        val config = session.config
        val targets = config.targets

        when (config.mode) {
            ClickMode.SINGLE -> {
                val target = targets.first()
                val interval = target.intervalMs
                var t = 0L
                var count = 0
                while (shouldContinue(session, target.repeat, count) &&
                    shouldContinueGlobal(session, count)
                ) {
                    yield(buildGesture(t, target, session))
                    t += interval
                    count++
                }
            }

            ClickMode.MULTI -> {
                var t = 0L
                val counts = IntArray(targets.size)
                var global = 0
                while (targets.indices.any { shouldContinue(session, targets[it].repeat, counts[it]) } &&
                    shouldContinueGlobal(session, global)
                ) {
                    for (i in targets.indices) {
                        val target = targets[i]
                        if (!shouldContinue(session, target.repeat, counts[i])) continue
                        // Wait this target's interval before it fires.
                        t += target.intervalMs
                        yield(buildGesture(t, target, session))
                        counts[i]++
                        global++
                    }
                }
            }

            ClickMode.SYNC -> {
                val cycleAdvance = targets.maxOf { it.intervalMs }
                val counts = IntArray(targets.size)
                var global = 0
                var t = 0L
                while (targets.indices.any { shouldContinue(session, targets[it].repeat, counts[it]) } &&
                    shouldContinueGlobal(session, global)
                ) {
                    // All targets fire at the same instant in this cycle.
                    for (i in targets.indices) {
                        val target = targets[i]
                        if (!shouldContinue(session, target.repeat, counts[i])) continue
                        yield(buildGesture(t, target, session))
                        counts[i]++
                        global++
                    }
                    t += cycleAdvance
                }
            }
        }
    }

    // --- helpers -------------------------------------------------------------

    /** Per-target repeat check. repeat == 0 means unlimited. */
    private fun shouldContinue(session: ClickSession, targetRepeat: Int, currentCount: Int): Boolean =
        targetRepeat == 0 || currentCount < targetRepeat

    /** Optional global cap (0 = unlimited). */
    private fun shouldContinueGlobal(session: ClickSession, globalCount: Int): Boolean =
        session.maxTotalRepeats == 0 || globalCount < session.maxTotalRepeats

    /** Builds one gesture for [target] at base time [baseT], applying jitter when enabled. */
    private fun buildGesture(
        baseT: Long,
        target: com.kunnn.totap.core.domain.model.Target,
        session: ClickSession,
    ): PlannedGesture {
        val jitter = if (session.config.antiDetection) target.jitter else JitterConfig()
        val fireAt = Jitter.applyTimeJitter(baseT, jitter, random)
        val (jx, jy) = Jitter.applyPositionJitter(target.x, target.y, jitter, random)
        val gesture: Gesture = when (val action = target.action) {
            is TargetAction.Tap -> Gesture.Tap(jx, jy, durationMs = 0L)
            is TargetAction.LongPress -> Gesture.Tap(jx, jy, durationMs = action.durationMs)
            is TargetAction.Swipe -> {
                val (ex, ey) = Jitter.applyPositionJitter(action.endX, action.endY, jitter, random)
                Gesture.Swipe(jx, jy, ex, ey, durationMs = action.durationMs)
            }
        }
        return PlannedGesture(fireAtMs = fireAt, gesture = gesture)
    }
}
