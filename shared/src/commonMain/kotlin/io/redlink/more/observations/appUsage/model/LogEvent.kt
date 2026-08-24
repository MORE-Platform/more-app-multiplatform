/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */

package io.redlink.more.observations.appUsage.model

enum class EventStorageMode {
    INSTANT,
    RANGE_START,
    IN_RANGE,
    RANGE_END
}

enum class EventFamily(val key: String) {
    APP_VISIBILITY("app_visibility"),
    VIEW("view_visibility")
}

enum class LogEvent(
    val key: String,
    val storageMode: EventStorageMode,
    val family: EventFamily? = null,
    val storeWithoutApproval: Boolean = false
) {
    APP_IN_FOREGROUND(
        key = "app_in_foreground",
        storageMode = EventStorageMode.RANGE_START,
        family = EventFamily.APP_VISIBILITY,
    ),
    APP_IN_BACKGROUND(
        key = "app_in_background",
        storageMode = EventStorageMode.RANGE_END,
        family = EventFamily.APP_VISIBILITY,
    ),
    VIEW_OPEN(
        key = "view_open",
        storageMode = EventStorageMode.RANGE_START,
        family = EventFamily.VIEW
    ),
    VIEW_CLOSED(
        key = "view_closed",
        storageMode = EventStorageMode.RANGE_END,
        family = EventFamily.VIEW
    ),
    BUTTON_PRESS(
        key = "button_press",
        storageMode = EventStorageMode.INSTANT,
    ),
    NOTIFICATION_INTERACTION(
        key = "notification_interaction",
        storageMode = EventStorageMode.INSTANT
    ),
    URL_OPEN(
        key = "url_open",
        storageMode = EventStorageMode.INSTANT
    ),
    APP_TRACKING_ACCEPTED(
        key = "app_tracking_accepted",
        storageMode = EventStorageMode.INSTANT,
        storeWithoutApproval = true
    ),
    OBSERVATION_EVENT(
        key = "observation_event",
        storageMode = EventStorageMode.INSTANT
    ),
    APP_TRACKING_DECLINED(
        key = "app_tracking_declined",
        storageMode = EventStorageMode.INSTANT,
        storeWithoutApproval = true
    ),
    BUTTON_CLICK(
        key = "button_click",
        storageMode = EventStorageMode.INSTANT
    ),
    DATE_SELECTION(
        key = "date_selection",
        storageMode = EventStorageMode.INSTANT
    ),
    STEP_VIEW_SUBMITTED(
        key = "step_view_submitted",
        storageMode = EventStorageMode.INSTANT
    ),
    NOTIFICATION_SHOWN(
        key = "notification_shown",
        storageMode = EventStorageMode.INSTANT
    ),
    NOTIFICATION_CLICKED(
        key = "notification_clicked",
        storageMode = EventStorageMode.INSTANT
    ),
    NOTIFICATION_DEEPLINK_OPENED(
        key = "notification_deeplink_opened",
        storageMode = EventStorageMode.INSTANT
    ),
    NOTIFICATION_DELIVERED(
        key = "notification_delivered",
        storageMode = EventStorageMode.INSTANT
    );

    fun aggregateKey(identifier: String): String =
        "${family?.name ?: key}:$identifier"

    fun isRangeEvent(): Boolean = storageMode != EventStorageMode.INSTANT

    fun isRangeStart(): Boolean = storageMode == EventStorageMode.RANGE_START

    fun inRange(): Boolean = storageMode == EventStorageMode.IN_RANGE
    fun isRangeEnd(): Boolean = storageMode == EventStorageMode.RANGE_END

    fun shouldStoreImmediately(): Boolean = storageMode == EventStorageMode.INSTANT

    fun matchingStartEvent(): LogEvent? = entries.firstOrNull {
        it.family == family && it.storageMode == EventStorageMode.RANGE_START
    }

    fun matchingEndEvent(): LogEvent? = entries.firstOrNull {
        it.family == family && it.storageMode == EventStorageMode.RANGE_END
    }

    fun canBeStored(openFamilies: Set<EventFamily>): Boolean {
        if (storeWithoutApproval) return true
        return when (storageMode) {
            EventStorageMode.INSTANT -> true
            EventStorageMode.RANGE_START, EventStorageMode.IN_RANGE -> false
            EventStorageMode.RANGE_END -> family != null && family in openFamilies
        }
    }
}