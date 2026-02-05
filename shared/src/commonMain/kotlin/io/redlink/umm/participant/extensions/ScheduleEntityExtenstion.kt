package io.redlink.umm.participant.extensions

import io.redlink.umm.participant.database.entities.NotificationEntity
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.models.NotificationTextKey

fun ScheduleEntity.toNotificationEntity(
    userFacing: Boolean,
    deepLink: String? = null
): NotificationEntity {
    return NotificationEntity(
        notificationId = "reminder_$scheduleId",
        title = observationTitle,
        notificationBody = NotificationTextKey.OBSERVATION_ACTIVATED.raw,
        timestamp = start,
        deepLink = deepLink,
        userFacing = userFacing
    )
}