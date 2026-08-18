package io.redlink.more.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant


sealed interface AppEvent {
    data object ScheduleHaveUpdated : AppEvent

    data object StudyHasUpdated : AppEvent

    data object DeviceBootComplete : AppEvent

    data class DataRefresh(
        val type: String,
        val observationId: String,
        val scheduleId: String? = null
    ) : AppEvent

    data class DayChanged(
        val date: LocalDate
    ) : AppEvent

    data class TimeZoneChanged(
        val timeZone: TimeZone
    ) : AppEvent

    data class SystemTimeChanged(
        val instant: Instant
    ) : AppEvent
}

object EventBus {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<AppEvent>(
        extraBufferCapacity = 64
    )

    val events = _events.asSharedFlow()

    inline fun <reified T : AppEvent> eventsOf(): Flow<T> = events.filterIsInstance<T>()

    fun publish(event: AppEvent) {
        scope.launch {
            _events.emit(event)
        }
    }

    fun tryPublish(event: AppEvent): Boolean = _events.tryEmit(event)
}
