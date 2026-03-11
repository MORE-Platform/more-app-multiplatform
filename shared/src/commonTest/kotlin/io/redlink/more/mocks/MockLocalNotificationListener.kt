package io.redlink.more.mocks

import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.services.notification.LocalNotificationListener

class MockLocalNotificationListener : LocalNotificationListener {
    val displayedNotifications = mutableListOf<NotificationEntity>()
    var clearScheduledCount = 0

    override fun displayNotification(notification: NotificationEntity, badgeCount: Int) {
        displayedNotifications.add(notification)
    }

    override fun clearScheduledNotifications(notifications: List<NotificationEntity>) {
        clearScheduledCount++
    }

    override fun deleteNotificationFromSystem(notificationId: String) {}
    override fun createNewFCMToken(onCompletion: (String) -> Unit) {
        onCompletion("new_fcm_token")
    }

    override fun clearNotifications() {}
    override fun deleteFCMToken() {}
    override fun updateBadgeCount(count: Int) {}
}
