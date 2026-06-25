package com.kunnn.totap.core.domain.planner

import com.kunnn.totap.core.domain.model.JitterConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class JitterTest {

    // ---- applyPositionJitter -------------------------------------------------

    @Test
    fun `position jitter off returns exact coordinates`() {
        val jitter = JitterConfig(positionJitterPx = 0)
        val (x, y) = Jitter.applyPositionJitter(baseX = 500f, baseY = 700f, jitter, Random)
        assertEquals(500f, x, 0.0001f)
        assertEquals(700f, y, 0.0001f)
    }

    @Test
    fun `position jitter stays within the configured radius`() {
        val maxPx = 20
        val jitter = JitterConfig(positionJitterPx = maxPx)
        val random = Random(42) // seeded: reproducible
        repeat(200) {
            val (x, y) = Jitter.applyPositionJitter(1000f, 1000f, jitter, random)
            val dx = x - 1000f
            val dy = y - 1000f
            assertTrue("x offset $dx out of range", dx in -maxPx.toFloat()..maxPx.toFloat())
            assertTrue("y offset $dy out of range", dy in -maxPx.toFloat()..maxPx.toFloat())
        }
    }

    @Test
    fun `position jitter is deterministic for a seeded Random`() {
        val jitter = JitterConfig(positionJitterPx = 10)
        val a = Jitter.applyPositionJitter(100f, 100f, jitter, Random(7))
        val b = Jitter.applyPositionJitter(100f, 100f, jitter, Random(7))
        assertEquals(a, b)
    }

    @Test
    fun `position jitter varies across calls`() {
        // Different seed each call -> at least one of several differs.
        val jitter = JitterConfig(positionJitterPx = 15)
        val results = (1..20).map { Jitter.applyPositionJitter(0f, 0f, jitter, Random(it * 31)) }.toSet()
        assertTrue("expected coordinate variety, got ${results.size}", results.size > 1)
    }

    // ---- applyTimeJitter -----------------------------------------------------

    @Test
    fun `time jitter off returns exact time`() {
        val jitter = JitterConfig(timeJitterMs = 0)
        val t = Jitter.applyTimeJitter(baseMs = 1000L, jitter, Random)
        assertEquals(1000L, t)
    }

    @Test
    fun `time jitter stays within range`() {
        val maxMs = 50L
        val jitter = JitterConfig(timeJitterMs = maxMs)
        val random = Random(99)
        repeat(200) {
            val t = Jitter.applyTimeJitter(5000L, jitter, random)
            assertTrue("time $t out of range", t in (5000L - maxMs)..(5000L + maxMs))
        }
    }

    @Test
    fun `time jitter is deterministic for a seeded Random`() {
        val jitter = JitterConfig(timeJitterMs = 30)
        val a = Jitter.applyTimeJitter(2000L, jitter, Random(3))
        val b = Jitter.applyTimeJitter(2000L, jitter, Random(3))
        assertEquals(a, b)
    }

    @Test
    fun `time jitter never goes negative when base is small`() {
        val jitter = JitterConfig(timeJitterMs = 100)
        val random = Random(1)
        repeat(100) {
            val t = Jitter.applyTimeJitter(baseMs = 5L, jitter, random)
            assertTrue("time $t went negative", t >= 0L)
        }
    }
}
