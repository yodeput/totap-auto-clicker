package com.kunnn.totap.core.domain.permission

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionCheckerTest {

    private fun statuses(vararg pairs: Pair<PermissionType, PermissionState>) =
        pairs.map { PermissionStatus(it.first, it.second) }

    @Test
    fun `canStart true when all three granted`() {
        assertTrue(
            PermissionChecker.canStart(
                statuses(
                    PermissionType.OVERLAY to PermissionState.GRANTED,
                    PermissionType.ACCESSIBILITY to PermissionState.GRANTED,
                    PermissionType.BATTERY to PermissionState.GRANTED,
                )
            )
        )
    }

    @Test
    fun `canStart false when overlay denied`() {
        assertFalse(
            PermissionChecker.canStart(
                statuses(
                    PermissionType.OVERLAY to PermissionState.DENIED,
                    PermissionType.ACCESSIBILITY to PermissionState.GRANTED,
                    PermissionType.BATTERY to PermissionState.GRANTED,
                )
            )
        )
    }

    @Test
    fun `canStart false when accessibility denied`() {
        assertFalse(
            PermissionChecker.canStart(
                statuses(
                    PermissionType.OVERLAY to PermissionState.GRANTED,
                    PermissionType.ACCESSIBILITY to PermissionState.DENIED,
                    PermissionType.BATTERY to PermissionState.GRANTED,
                )
            )
        )
    }

    @Test
    fun `canStart false when battery denied`() {
        assertFalse(
            PermissionChecker.canStart(
                statuses(
                    PermissionType.OVERLAY to PermissionState.GRANTED,
                    PermissionType.ACCESSIBILITY to PermissionState.GRANTED,
                    PermissionType.BATTERY to PermissionState.DENIED,
                )
            )
        )
    }

    @Test
    fun `canStart false when any permission is UNKNOWN`() {
        assertFalse(
            PermissionChecker.canStart(
                statuses(
                    PermissionType.OVERLAY to PermissionState.GRANTED,
                    PermissionType.ACCESSIBILITY to PermissionState.UNKNOWN,
                    PermissionType.BATTERY to PermissionState.GRANTED,
                )
            )
        )
    }

    @Test
    fun `canStart false when a permission is missing entirely`() {
        // Only overlay + accessibility present (battery missing).
        assertFalse(
            PermissionChecker.canStart(
                statuses(
                    PermissionType.OVERLAY to PermissionState.GRANTED,
                    PermissionType.ACCESSIBILITY to PermissionState.GRANTED,
                )
            )
        )
    }
}
