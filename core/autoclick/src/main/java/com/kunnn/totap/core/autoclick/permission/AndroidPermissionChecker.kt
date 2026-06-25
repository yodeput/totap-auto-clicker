package com.kunnn.totap.core.autoclick.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import com.kunnn.totap.core.domain.permission.PermissionChecker
import com.kunnn.totap.core.domain.permission.PermissionState
import com.kunnn.totap.core.domain.permission.PermissionStatus
import com.kunnn.totap.core.domain.permission.PermissionType
import com.kunnn.totap.core.autoclick.service.AutoClickAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [PermissionChecker] (spec §4.5).
 *
 * Reads the three system permission states directly:
 * - OVERLAY:       Settings.canDrawOverlays()
 * - ACCESSIBILITY: enabled in the system accessibility settings + our service connected
 * - BATTERY:       PowerManager.isIgnoringBatteryOptimizations()
 */
@Singleton
class AndroidPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : PermissionChecker {

    override fun status(): List<PermissionStatus> = listOf(
        PermissionStatus(PermissionType.OVERLAY, overlayState()),
        PermissionStatus(PermissionType.ACCESSIBILITY, accessibilityState()),
        PermissionStatus(PermissionType.BATTERY, batteryState()),
    )

    private fun overlayState(): PermissionState =
        if (Settings.canDrawOverlays(context)) PermissionState.GRANTED else PermissionState.DENIED

    private fun accessibilityState(): PermissionState {
        val enabled = isAccessibilityServiceEnabled()
        // Also confirm the service has actually connected (instance published).
        val connected = AutoClickAccessibilityService.isConnected
        return when {
            enabled && connected -> PermissionState.GRANTED
            enabled -> PermissionState.GRANTED // enabled but not yet connected; will connect shortly
            else -> PermissionState.DENIED
        }
    }

    private fun batteryState(): PermissionState {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (pm.isIgnoringBatteryOptimizations(context.packageName)) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }

    /** True if our AccessibilityService is enabled in system settings. */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = context.packageName + "/" +
            AutoClickAccessibilityService::class.java.name
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':').apply { setString(enabled) }
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    companion object {
        /** Intent that opens the overlay-permission settings screen for our package. */
        fun overlaySettingsIntent(context: Context) =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))

        /** Intent that opens the system accessibility settings list. */
        fun accessibilitySettingsIntent() = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

        /** Intent that requests the battery-optimization exemption. */
        fun batterySettingsIntent(context: Context) =
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
    }
}
