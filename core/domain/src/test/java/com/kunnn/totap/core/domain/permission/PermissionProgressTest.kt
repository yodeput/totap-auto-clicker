package com.kunnn.totap.core.domain.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionProgressTest {

    private fun perms(vararg pairs: Pair<PermissionType, PermissionState>) =
        pairs.map { PermissionStatus(it.first, it.second) }

    @Test
    fun `none granted yields 0 fraction and remaining 3`() {
        val p = PermissionProgress.from(
            perms(
                PermissionType.OVERLAY to PermissionState.DENIED,
                PermissionType.ACCESSIBILITY to PermissionState.DENIED,
                PermissionType.BATTERY to PermissionState.DENIED,
            )
        )
        assertEquals(0, p.granted)
        assertEquals(3, p.total)
        assertEquals(0f, p.fraction, 0.0001f)
        assertEquals(3, p.remaining)
        assertFalse(p.allGranted)
    }

    @Test
    fun `two of three granted yields 0_66 fraction and remaining 1`() {
        val p = PermissionProgress.from(
            perms(
                PermissionType.OVERLAY to PermissionState.GRANTED,
                PermissionType.ACCESSIBILITY to PermissionState.GRANTED,
                PermissionType.BATTERY to PermissionState.DENIED,
            )
        )
        assertEquals(2, p.granted)
        assertEquals(1, p.remaining)
        assertEquals(2f / 3f, p.fraction, 0.001f)
        assertFalse(p.allGranted)
    }

    @Test
    fun `all three granted yields fraction 1 and allGranted true`() {
        val p = PermissionProgress.from(
            perms(
                PermissionType.OVERLAY to PermissionState.GRANTED,
                PermissionType.ACCESSIBILITY to PermissionState.GRANTED,
                PermissionType.BATTERY to PermissionState.GRANTED,
            )
        )
        assertEquals(3, p.granted)
        assertEquals(0, p.remaining)
        assertEquals(1f, p.fraction, 0.0001f)
        assertTrue(p.allGranted)
    }

    @Test
    fun `UNKNOWN counts as not granted`() {
        val p = PermissionProgress.from(
            perms(
                PermissionType.OVERLAY to PermissionState.GRANTED,
                PermissionType.ACCESSIBILITY to PermissionState.UNKNOWN,
                PermissionType.BATTERY to PermissionState.GRANTED,
            )
        )
        assertEquals(2, p.granted)
        assertEquals(1, p.remaining)
        assertFalse(p.allGranted)
    }
}
