package com.kunnn.totap.core.domain.autoclick

import com.kunnn.totap.core.domain.model.ClickSession

/**
 * The runtime state of the click engine, surfaced to the UI via
 * [AutoClickController.status]. Idle until [AutoClickController.start] is called.
 */
sealed interface EngineStatus {
    /** Engine is not running. */
    data object Idle : EngineStatus

    /** Engine is dispatching gestures for [session]. */
    data class Running(val session: ClickSession, val gesturesFired: Int = 0) : EngineStatus

    /** Engine is paused; can be resumed. Holds the active [session]. */
    data class Paused(val session: ClickSession) : EngineStatus

    /** Engine stopped due to an error (e.g. accessibility service disconnected). */
    data class Error(val message: String) : EngineStatus
}
