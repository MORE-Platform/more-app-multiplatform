/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.mocks

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.navigation.DeeplinkManager
import io.redlink.more.navigation.model.DeepLinkData
import io.redlink.more.observations.ObservationFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockDeeplinkManager(repos: MainRepository) : DeeplinkManager {
    override val observationFactory: ObservationFactory = MockObservationFactory(repos)

    override fun addAvailableDeepLinks(deepLinks: Set<String>) {}

    override fun setProtocol(protocolReplacement: String?) {}

    override fun setHost(hostReplacement: String?) {}

    override fun getNotificationViewDeepLink(notificationId: String): Flow<DeepLinkData?> =
        flowOf(DeepLinkData("app://more/notifications?notificationId=$notificationId"))

    override fun modifyDeepLink(deepLink: String?): Flow<DeepLinkData?> =
        flowOf(deepLink?.let { DeepLinkData(it) })

    override fun modifyDeepLink(
        deepLink: String?,
        newState: (DeepLinkData?) -> Unit
    ): Closeable = object : Closeable {
        override fun close() {}
    }

    override fun validateRoute(deepLink: String): Boolean = true

    override fun createDeeplinkForSchedule(
        schedule: ScheduleEntity,
        baseDeeplink: String?
    ): String = "app://more/task-details?scheduleId=${schedule.scheduleId}"
}
