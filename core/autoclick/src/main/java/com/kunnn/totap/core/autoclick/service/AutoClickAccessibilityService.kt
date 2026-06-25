package com.kunnn.totap.core.autoclick.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.kunnn.totap.core.domain.model.Gesture
import java.lang.ref.WeakReference

/**
 * The system-instantiated AccessibilityService that owns [dispatchGesture] (spec §4.2).
 *
 * We don't read screen content — this service only *dispatches* gestures. The
 * running instance is published via [instance] in [onServiceConnected] so the
 * [AutoClickControllerImpl][com.kunnn.totap.core.autoclick.controller.AutoClickControllerImpl]
 * can reach it without being coupled to the service lifecycle.
 */
@RequiresApi(Build.VERSION_CODES.N)
class AutoClickAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't read events; we only dispatch gestures.
    }

    override fun onInterrupt() {
        // No-op.
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    /**
     * Dispatches a [Gesture] and invokes [onComplete] when it finishes.
     * Returns true if the gesture was dispatched.
     */
    fun dispatch(gesture: Gesture, onComplete: (Boolean) -> Unit): Boolean {
        val path = Path().apply {
            when (gesture) {
                is Gesture.Tap -> moveTo(gesture.x, gesture.y)
                is Gesture.Swipe -> {
                    moveTo(gesture.startX, gesture.startY)
                    lineTo(gesture.endX, gesture.endY)
                }
            }
        }
        val stroke = GestureDescription.StrokeDescription(
            path,
            /* startTime= */ 0L,
            /* duration= */ if (gesture.durationMs <= 0L) MIN_TAP_DURATION_MS else gesture.durationMs,
        )
        val description = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(description, object : GestureResultCallback() {
            override fun onCompleted(description: GestureDescription?) = onComplete(true)
            override fun onCancelled(description: GestureDescription?) = onComplete(false)
        }, null)
    }

    companion object {
        /** The shortest reliable gesture duration (~1 frame). Pure taps below this floor can be dropped. */
        const val MIN_TAP_DURATION_MS = 1L

        /** The currently-running service instance, or null when not connected/enabled. */
        @Volatile
        var instance: WeakReference<AutoClickAccessibilityService>? = null
            private set

        /** True when the system has instantiated and connected the service. */
        val isConnected: Boolean get() = instance?.get() != null
    }
}
