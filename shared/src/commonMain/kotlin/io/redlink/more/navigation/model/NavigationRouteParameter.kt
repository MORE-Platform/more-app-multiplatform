package io.redlink.more.navigation.model

enum class NavigationRouteParameter(val key: String) {
    SCHEDULE_ID("scheduleId"),
    OBSERVATION_ID("observationId"),
    NOTIFICATION_ID("notificationId"),
    SCHEDULE_LIST_TYPE("scheduleListType");

    companion object {
        fun fromKey(key: String) = entries.find { it.key == key }
    }
}