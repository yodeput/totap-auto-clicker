package com.kunnn.totap.core.autoclick.controller

import android.content.Context
import com.kunnn.totap.core.autoclick.service.AutoClickAccessibilityService
import com.kunnn.totap.core.autoclick.service.AutoClickForegroundService
import com.kunnn.totap.core.domain.autoclick.AutoClickController
import com.kunnn.totap.core.domain.autoclick.EngineStatus
import com.kunnn.totap.core.domain.model.ClickSession
import com.kunnn.totap.core.domain.planner.GesturePlanner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * The real [AutoClickController] (spec §4.2). Bridges the pure [GesturePlanner]
 * to the system [AutoClickAccessibilityService].
 *
 * Concurrency model: a single engine [Job] lives on an app-scoped coroutine
 * dispatcher. [start] cancels any prior run under a [mutex]; [stop] cancels it.
 * Each planned gesture is dispatched at its [PlannedGesture.fireAtMs] relative
 * to run start, with the planner's lazy sequence feeding one gesture at a time.
 */
@Singleton
class AutoClickControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AutoClickController {

    private val planner = GesturePlanner(random = Random.Default)
    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    private val _status = MutableStateFlow<EngineStatus>(EngineStatus.Idle)
    override val status: StateFlow<EngineStatus> = _status.asStateFlow()

    private var runJob: Job? = null

    override suspend fun start(session: ClickSession) {
        mutex.withLock {
            runJob?.cancel()
            // Ensure the foreground service is alive before we dispatch.
            AutoClickForegroundService.start(context)
            _status.value = EngineStatus.Running(session)
            runJob = scope.launch { runEngine(session) }
        }
    }

    override suspend fun resume() {
        val s = _status.value
        if (s is EngineStatus.Paused) {
            _status.value = EngineStatus.Running(s.session)
            runJob = scope.launch { runEngine(s.session) }
        }
    }

    override suspend fun pause() {
        mutex.withLock {
            runJob?.cancel()
            val s = _status.value
            if (s is EngineStatus.Running) _status.value = EngineStatus.Paused(s.session)
        }
    }

    override suspend fun stop() {
        mutex.withLock {
            runJob?.cancel()
            runJob = null
            AutoClickForegroundService.stop(context)
            _status.value = EngineStatus.Idle
        }
    }

    private suspend fun runEngine(session: ClickSession) {
        val service = AutoClickAccessibilityService.instance?.get()
        if (service == null) {
            _status.value = EngineStatus.Error("Accessibility service not connected")
            return
        }
        val runStartMs = System.currentTimeMillis()
        var fired = 0
        try {
            for (planned in planner.plan(session)) {
                // Sleep until the planned fire instant (relative to run start).
                val now = System.currentTimeMillis() - runStartMs
                val wait = planned.fireAtMs - now
                if (wait > 0) delay(wait)
                service.dispatch(planned.gesture) { /* completed */ }
                fired++
                _status.value = EngineStatus.Running(session, fired)
            }
            // Sequence ended (repeat limits reached) -> return to idle.
            _status.value = EngineStatus.Idle
            AutoClickForegroundService.stop(context)
        } catch (_: kotlinx.coroutines.CancellationException) {
            // Paused or stopped; status already updated by pause()/stop().
        }
    }
}
