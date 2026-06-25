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
}
