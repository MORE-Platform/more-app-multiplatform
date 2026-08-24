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
package io.redlink.more.mocks

import io.redlink.more.database.repository.MainRepository
import io.redlink.more.navigation.DeeplinkManager
import io.redlink.more.navigation.model.DeepLinkData
import io.redlink.more.services.network.NetworkService
import io.redlink.more.services.notification.LocalNotificationListener
import io.redlink.more.services.notification.NotificationActionHandler
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.services.store.SharedStorageRepository

class MockNotificationManager(
    repository: MainRepository,
    localNotificationListener: LocalNotificationListener,
    networkService: NetworkService,
    deeplinkManager: DeeplinkManager,
    sharedStorageRepository: SharedStorageRepository
) : NotificationManager(
    repository,
    localNotificationListener,
    networkService,
    deeplinkManager,
    sharedStorageRepository
) {
    var handleNotificationInteractionCalled = false

    override fun handleNotificationInteraction(
        notificationId: String,
        deepLink: String?,
        handler: (NotificationActionHandler, DeepLinkData?) -> Unit
    ) {
        handleNotificationInteractionCalled = true
    }
}