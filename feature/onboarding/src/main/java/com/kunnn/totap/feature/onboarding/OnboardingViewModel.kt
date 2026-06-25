package com.kunnn.totap.feature.onboarding

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.kunnn.totap.core.autoclick.permission.AndroidPermissionChecker
import com.kunnn.totap.core.domain.permission.PermissionChecker
import com.kunnn.totap.core.domain.permission.PermissionProgress
import com.kunnn.totap.core.domain.permission.PermissionStatus
import com.kunnn.totap.core.domain.permission.PermissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Reactive permission state for the onboarding screen.
 *
 * Key UX fix: permission state re-reads on every [refresh] — which the screen
 * calls from a lifecycle observer (onResume), so the cards flip to "Granted"
 * the instant the user returns from system settings. No manual polling needed.
 */
data class OnboardingState(
    val permissions: List<PermissionStatus> = emptyList(),
    val progress: PermissionProgress = PermissionProgress(0, 3),
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application: Application,
    private val checker: PermissionChecker,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        refresh()
    }

    /**
     * Re-read all permission states from the system and recompute progress.
     * Idempotent and cheap; safe to call on every resume.
     */
    fun refresh() {
        val perms = checker.status()
        _state.value = OnboardingState(
            permissions = perms,
            progress = PermissionProgress.from(perms),
        )
    }

    /** True when every required permission is granted. */
    val allGranted: Boolean get() = _state.value.progress.allGranted

    /** Opens the system settings screen for the given permission type. */
    fun openSettingsFor(type: PermissionType) {
        val ctx = getApplication<Application>()
        val intent: Intent = when (type) {
            PermissionType.OVERLAY -> AndroidPermissionChecker.overlaySettingsIntent(ctx)
            PermissionType.ACCESSIBILITY -> AndroidPermissionChecker.accessibilitySettingsIntent()
            PermissionType.BATTERY -> AndroidPermissionChecker.batterySettingsIntent(ctx)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(intent)
    }
}
