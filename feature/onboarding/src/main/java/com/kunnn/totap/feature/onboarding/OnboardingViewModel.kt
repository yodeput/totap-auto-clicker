package com.kunnn.totap.feature.onboarding

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kunnn.totap.core.autoclick.permission.AndroidPermissionChecker
import com.kunnn.totap.core.domain.permission.PermissionChecker
import com.kunnn.totap.core.domain.permission.PermissionStatus
import com.kunnn.totap.core.domain.permission.PermissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingState(
    val permissions: List<PermissionStatus> = emptyList(),
    val allGranted: Boolean = false,
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

    /** Re-read all permission states. Call on screen resume (after returning from settings). */
    fun refresh() {
        val perms = checker.status()
        _state.value = OnboardingState(perms, PermissionChecker.canStart(perms))
    }

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
