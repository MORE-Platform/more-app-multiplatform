package io.redlink.umm.participant.services

import io.github.aakira.napier.Napier
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.models.ScheduleState
import io.redlink.umm.participant.services.notification.NotificationManager
import io.redlink.umm.participant.services.store.SharedStorageRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.math.max

class ObservationService(
    private val sharedStorageRepository: SharedStorageRepository,
    private val repositories: MainRepository,
    private val notificationManager: NotificationManager
) {
    suspend fun scheduleObservationReminder() {
        Napier.i(tag = "ObservationService::scheduleObservationReminder") { "Starting scheduleObservationReminder()" }
        val scheduledNotificationCount = repositories.notification.scheduledNotificationCount()
        if (scheduledNotificationCount >= MAX_SCHEDULE_COUNT) {
            Napier.i(tag = "ObservationService::scheduleObservationReminder") { "Already scheduled enough observation reminders: $scheduledNotificationCount" }
            return
        }
        val now = Clock.System.now()
        val daysFromNow: Instant =
            now.plus(DateTimePeriod(days = DAYS_INTO_FUTURE), TimeZone.currentSystemDefault())
        val lastCollection = max(
            sharedStorageRepository.load(OBSERVATION_REMINDER_LATEST_TIMESTAMP_KEY, 0L),
            now.epochSeconds
        )
        val minTimestamp = Instant.fromEpochSeconds(lastCollection)
        if (minTimestamp >= daysFromNow) {
            return
        }
        repositories.schedule.getVisibleSchedulesUntilDate(
            setOf(ScheduleState.DEACTIVATED),
            minTimestamp,
            daysFromNow,
            MAX_SCHEDULE_COUNT - scheduledNotificationCount
        ).firstOrNull()
            ?.filter { it.start != null }
            ?.let { schedules: List<ScheduleEntity> ->
                if (schedules.isNotEmpty()) {
                    notificationManager.scheduleObservationReminders(schedules)
                    val collectionTimestamp =
                        schedules.mapNotNull { it.start }.maxOrNull()
                            ?: Clock.System.now().epochSeconds
                    sharedStorageRepository.store(
                        OBSERVATION_REMINDER_LATEST_TIMESTAMP_KEY,
                        collectionTimestamp
                    )
                }
            }

    }

    suspend fun clearReminders() {
        sharedStorageRepository.remove(OBSERVATION_REMINDER_LATEST_TIMESTAMP_KEY)
        notificationManager.clearScheduledNotifications()
    }

    companion object {
        private const val OBSERVATION_REMINDER_LATEST_TIMESTAMP_KEY =
            "observation_reminder_timestamp"
        private const val MAX_SCHEDULE_COUNT = 10
        private const val DAYS_INTO_FUTURE = 3
    }
}