package com.kunnn.totap.core.domain.permission

/**
 * Reactive, derived progress over a list of permission statuses (spec §6.2 onboarding UX).
 *
 * Pure: computed from statuses in the ViewModel; unit-testable.
 */
data class PermissionProgress(
    val granted: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total == 0) 0f else granted.toFloat() / total.toFloat()
    val allGranted: Boolean get() = total > 0 && granted == total
    val remaining: Int get() = (total - granted).coerceAtLeast(0)

    companion object {
        fun from(statuses: List<PermissionStatus>): PermissionProgress {
            val granted = statuses.count { it.isGranted }
            return PermissionProgress(granted = granted, total = PermissionType.entries.size)
        }
    }
}
