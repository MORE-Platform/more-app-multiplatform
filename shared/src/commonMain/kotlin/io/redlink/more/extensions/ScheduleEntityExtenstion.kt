package io.redlink.more.extensions

import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.models.NotificationTextKey

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