/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
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