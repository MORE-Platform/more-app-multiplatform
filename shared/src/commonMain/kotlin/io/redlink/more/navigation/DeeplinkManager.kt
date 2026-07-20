package io.redlink.more.navigation

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.navigation.model.DeepLinkData
import io.redlink.more.observations.ObservationFactory
import kotlinx.coroutines.flow.Flow

interface DeeplinkManager {
    val observationFactory: ObservationFactory

    fun addAvailableDeepLinks(deepLinks: Set<String>)

    fun setProtocol(protocolReplacement: String?)

    fun setHost(hostReplacement: String?)

    fun getNotificationViewDeepLink(
        notificationId: String,
    ): Flow<DeepLinkData?>

    fun modifyDeepLink(
        deepLink: String?,
    ): Flow<DeepLinkData?>

    fun modifyDeepLink(
        deepLink: String?,
        newState: (DeepLinkData?) -> Unit
    ): Closeable

    fun validateRoute(deepLink: String): Boolean

    fun createDeeplinkForSchedule(
        schedule: ScheduleEntity,
        baseDeeplink: String? = null
    ): String
}