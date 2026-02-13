package io.redlink.umm.participant.services

import io.github.aakira.napier.Napier
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.models.ScheduleState
import io.redlink.umm.participant.services.notification.NotificationManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

class ObservationService(
    private val repositories: MainRepository,
    private val notificationManager: NotificationManager,
    private val schedulingLimit: Int? = null
) {
    suspend fun scheduleObservationReminder() {
        Napier.i(tag = "ObservationService::scheduleObservationReminder") { "Starting scheduleObservationReminder()" }
        val now = Clock.System.now()
        val daysFromNow: Instant =
            now.plus(DateTimePeriod(days = DAYS_INTO_FUTURE), TimeZone.currentSystemDefault())
        repositories.schedule.getSchedulesWithReminder(
            setOf(ScheduleState.DEACTIVATED),
            now,
            daysFromNow,
            schedulingLimit ?: MAX_SCHEDULE_COUNT
        ).firstOrNull()
            ?.filter { it.start != null }
            ?.let { schedules: List<ScheduleEntity> ->
                if (schedules.isNotEmpty()) {
                    notificationManager.scheduleObservationReminders(schedules)
                    Napier.i(tag = "ObservationService::scheduleObservationReminder") { "Scheduled ${schedules.size} notifications!" }
                } else {
                    Napier.i(tag = "ObservationService::scheduleObservationReminder") { "No schedules with reminder found." }
                }
            }
    }

    suspend fun rescheduleObservationRemindersAfterBoot() {
        Napier.i(tag = "ObservationService::rescheduleObservationRemindersAfterBoot") { "Starting rescheduleObservationRemindersAfterBoot()" }
        val scheduledNotifications = repositories.notification.scheduledNotifications()
        if (scheduledNotifications.isNotEmpty()) {
            notificationManager.rescheduleNotifications(scheduledNotifications)
            Napier.i(tag = "ObservationService::rescheduleObservationRemindersAfterBoot") { "Rescheduled ${scheduledNotifications.size} notifications after boot." }
        }
    }

    suspend fun clearReminders() {
        notificationManager.clearScheduledNotifications()
    }

    companion object {
        private const val MAX_SCHEDULE_COUNT = 15
        private const val DAYS_INTO_FUTURE = 7
    }
}