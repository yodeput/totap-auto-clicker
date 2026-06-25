package com.kunnn.totap.core.domain.planner

import com.kunnn.totap.core.domain.model.ClickConfig
import com.kunnn.totap.core.domain.model.ClickMode
import com.kunnn.totap.core.domain.model.ClickSession
import com.kunnn.totap.core.domain.model.Gesture
import com.kunnn.totap.core.domain.model.Target
import com.kunnn.totap.core.domain.model.TargetAction
import org.junit.Assert.assertEquals
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
}
