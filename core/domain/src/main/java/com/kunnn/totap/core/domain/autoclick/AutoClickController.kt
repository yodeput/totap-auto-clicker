package com.kunnn.totap.core.domain.autoclick

import com.kunnn.totap.core.domain.model.ClickSession
import kotlinx.coroutines.flow.Flow

/**
 * The single entry point features use to drive the click engine (spec §4.2).
 *
 * Implementations live in `:core:autoclick`; this interface is the isolation
 * boundary — features never touch the AccessibilityService directly.
 *
 * All permission checks happen *before* [start]; callers should consult
 * [com.kunnn.totap.core.domain.permission.PermissionChecker] first.
 */
interface AutoClickController {
    /** Starts firing gestures for [session]. Replaces any running session. */
    suspend fun start(session: ClickSession)

    /** Resumes after a pause. No-op if idle or running. */
    suspend fun resume()

    /** Pauses the current run; gestures stop but the session is retained. */
    suspend fun pause()

    /** Stops the engine and clears the active session. */
    suspend fun stop()

    /** Observable engine state for the UI. */
    val status: Flow<EngineStatus>
}
