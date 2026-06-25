package com.kunnn.totap.core.domain.planner

import com.kunnn.totap.core.domain.model.ClickConfig
import com.kunnn.totap.core.domain.model.ClickMode
import com.kunnn.totap.core.domain.model.ClickSession
import com.kunnn.totap.core.domain.model.Gesture
import com.kunnn.totap.core.domain.model.JitterConfig
import com.kunnn.totap.core.domain.model.Target
import com.kunnn.totap.core.domain.model.TargetAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GesturePlannerTest {

    private fun planner() = GesturePlanner(random = kotlin.random.Random(0))

    // ---- SINGLE mode ---------------------------------------------------------

    @Test
    fun `SINGLE mode fires only the first target repeatedly on its interval`() {
        val target = Target(id = "t1", x = 100f, y = 200f, action = TargetAction.Tap, intervalMs = 50L)
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target)))

        val gestures = planner().plan(session).take(3).toList()

        assertEquals(3, gestures.size)
        // Fires at 0, 50, 100 (interval is the delay BEFORE each fire).
        assertEquals(0L, gestures[0].fireAtMs)
        assertEquals(50L, gestures[1].fireAtMs)
        assertEquals(100L, gestures[2].fireAtMs)
    }

    @Test
    fun `SINGLE mode ignores other targets`() {
        val t1 = Target(id = "t1", x = 10f, y = 10f, intervalMs = 30L)
        val t2 = Target(id = "t2", x = 999f, y = 999f, intervalMs = 30L)
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(t1, t2)))

        val gestures = planner().plan(session).take(4).toList()

        // Every gesture must be a tap at t1's coordinates, never t2's.
        gestures.forEach { g ->
            val tap = g.gesture as Gesture.Tap
            assertEquals(10f, tap.x, 0.001f)
            assertEquals(10f, tap.y, 0.001f)
        }
    }

    @Test
    fun `SINGLE mode produces an instantaneous tap (durationMs 0)`() {
        val target = Target(id = "t1", x = 100f, y = 100f, action = TargetAction.Tap)
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target)))

        val g = planner().plan(session).first()

        val tap = g.gesture as Gesture.Tap
        assertEquals(0L, tap.durationMs)
    }

    // ---- MULTI mode ----------------------------------------------------------

    @Test
    fun `MULTI mode fires targets round-robin`() {
        val t1 = Target(id = "t1", x = 10f, y = 10f, intervalMs = 100L)
        val t2 = Target(id = "t2", x = 20f, y = 20f, intervalMs = 100L)
        val session = ClickSession(ClickConfig(ClickMode.MULTI, listOf(t1, t2)))

        val xs = planner().plan(session).take(4).toList().map { (it.gesture as Gesture.Tap).x }

        // t1, t2, t1, t2 ...
        assertEquals(listOf(10f, 20f, 10f, 20f), xs)
    }

    @Test
    fun `MULTI mode waits each targets interval before its fire`() {
        // t1 waits 100ms, t2 waits 30ms. Cumulative: t1@100, t2@130, t1@230, t2@260.
        val t1 = Target(id = "t1", x = 10f, y = 10f, intervalMs = 100L)
        val t2 = Target(id = "t2", x = 20f, y = 20f, intervalMs = 30L)
        val session = ClickSession(ClickConfig(ClickMode.MULTI, listOf(t1, t2)))

        val times = planner().plan(session).take(4).toList().map { it.fireAtMs }

        assertEquals(listOf(100L, 130L, 230L, 260L), times)
    }

    // ---- SYNC mode -----------------------------------------------------------

    @Test
    fun `SYNC mode fires all targets at the same instant per cycle`() {
        val t1 = Target(id = "t1", x = 10f, y = 10f, intervalMs = 100L)
        val t2 = Target(id = "t2", x = 20f, y = 20f, intervalMs = 200L)
        val session = ClickSession(ClickConfig(ClickMode.SYNC, listOf(t1, t2)))

        val cycle0 = planner().plan(session).take(2).toList()

        // Both fire at t=0 in the first cycle.
        assertEquals(0L, cycle0[0].fireAtMs)
        assertEquals(0L, cycle0[1].fireAtMs)
    }

    @Test
    fun `SYNC mode cycle advances by the longest interval`() {
        val t1 = Target(id = "t1", x = 10f, y = 10f, intervalMs = 100L)
        val t2 = Target(id = "t2", x = 20f, y = 20f, intervalMs = 200L)
        val session = ClickSession(ClickConfig(ClickMode.SYNC, listOf(t1, t2)))

        val times = planner().plan(session).take(4).toList().map { it.fireAtMs }

        // cycle0: both @ 0; cycle1: both @ 200 (max interval).
        assertEquals(listOf(0L, 0L, 200L, 200L), times)
    }

    // ---- LongPress + Swipe actions ------------------------------------------

    @Test
    fun `LongPress action produces a tap with the hold duration`() {
        val target = Target(
            id = "t1", x = 50f, y = 60f,
            action = TargetAction.LongPress(durationMs = 500L),
        )
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target)))

        val tap = planner().plan(session).first().gesture as Gesture.Tap

        assertEquals(50f, tap.x, 0.001f)
        assertEquals(60f, tap.y, 0.001f)
        assertEquals(500L, tap.durationMs)
    }

    @Test
    fun `Swipe action produces a drag from start to end over the duration`() {
        val target = Target(
            id = "t1", x = 10f, y = 10f,
            action = TargetAction.Swipe(endX = 100f, endY = 200f, durationMs = 300L),
        )
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target)))

        val swipe = planner().plan(session).first().gesture as Gesture.Swipe

        assertEquals(10f, swipe.startX, 0.001f)
        assertEquals(10f, swipe.startY, 0.001f)
        assertEquals(100f, swipe.endX, 0.001f)
        assertEquals(200f, swipe.endY, 0.001f)
        assertEquals(300L, swipe.durationMs)
    }

    // ---- Repeat limits -------------------------------------------------------

    @Test
    fun `per-target repeat stops the target after its count in MULTI mode`() {
        val t1 = Target(id = "t1", x = 10f, y = 10f, intervalMs = 10L, repeat = 2)
        val t2 = Target(id = "t2", x = 20f, y = 20f, intervalMs = 10L, repeat = 1)
        val session = ClickSession(ClickConfig(ClickMode.MULTI, listOf(t1, t2)))

        val xs = planner().plan(session).toList().map { (it.gesture as Gesture.Tap).x }

        // t1 fires twice (10,10), t2 once (20), then t1 keeps going... but t1 cap=2, t2 cap=1.
        // Round 1: t1, t2. Round 2: t1 only (t2 capped). Then both capped -> stop.
        assertEquals(listOf(10f, 20f, 10f), xs)
    }

    @Test
    fun `global maxTotalRepeats caps the whole sequence`() {
        val target = Target(id = "t1", x = 1f, y = 1f, intervalMs = 5L)
        val session = ClickSession(
            ClickConfig(ClickMode.SINGLE, listOf(target)),
            maxTotalRepeats = 3,
        )

        val count = planner().plan(session).toList().size

        assertEquals(3, count)
    }

    // ---- Jitter integration --------------------------------------------------

    @Test
    fun `antiDetection disabled yields exact coordinates and times`() {
        val target = Target(
            id = "t1", x = 100f, y = 200f, intervalMs = 50L,
            jitter = JitterConfig(positionJitterPx = 20, timeJitterMs = 30),
        )
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target), antiDetection = false))

        val gestures = planner().plan(session).take(2).toList()

        assertEquals(listOf(0L, 50L), gestures.map { it.fireAtMs })
        gestures.forEach { g ->
            val tap = g.gesture as Gesture.Tap
            assertEquals(100f, tap.x, 0.0001f)
            assertEquals(200f, tap.y, 0.0001f)
        }
    }

    @Test
    fun `antiDetection enabled nudges coordinates within jitter radius`() {
        val maxPx = 25
        val target = Target(
            id = "t1", x = 500f, y = 500f, intervalMs = 50L,
            jitter = JitterConfig(positionJitterPx = maxPx, timeJitterMs = 0),
        )
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target), antiDetection = true))

        val taps = planner().plan(session).take(20).toList().map { it.gesture as Gesture.Tap }

        taps.forEach { tap ->
            assertTrue("x ${tap.x} out of range", tap.x in (500f - maxPx)..(500f + maxPx))
            assertTrue("y ${tap.y} out of range", tap.y in (500f - maxPx)..(500f + maxPx))
        }
        // And there must be variety (jitter is actually being applied).
        assertTrue("expected x variety", taps.map { it.x }.toSet().size > 1)
    }

    @Test
    fun `antiDetection enabled keeps fire times within time jitter`() {
        val maxMs = 40L
        val target = Target(
            id = "t1", x = 100f, y = 100f, intervalMs = 100L,
            jitter = JitterConfig(positionJitterPx = 0, timeJitterMs = maxMs),
        )
        val session = ClickSession(ClickConfig(ClickMode.SINGLE, listOf(target), antiDetection = true))

        val times = planner().plan(session).take(15).toList().map { it.fireAtMs }

        // Base times are 0, 100, 200...; jittered must stay within ±maxMs of each base.
        times.forEachIndexed { i, t ->
            val base = i * 100L
            assertTrue("time $t vs base $base", t in (base - maxMs)..(base + maxMs))
        }
    }
}
