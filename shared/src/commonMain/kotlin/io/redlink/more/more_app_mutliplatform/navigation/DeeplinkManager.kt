package io.redlink.more.more_app_mutliplatform.navigation

import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.extractRouteFromDeepLink
import io.redlink.more.more_app_mutliplatform.extensions.mapQueryParams
import io.redlink.more.more_app_mutliplatform.models.NotificationStatusType
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class DeeplinkManager(private val observationFactory: ObservationFactory) {
    private val deepLinks = mutableSetOf<String>()
    private val scheduleRepository = ScheduleRepository()
    private val observationRepository = ObservationRepository()

    fun addAvailableDeepLinks(deepLinks: Set<String>) {
        this.deepLinks.addAll(deepLinks)
    }

    fun checkIfCompletedOrRead(
        deepLink: String?
    ): Flow<NotificationStatusType?> = flow {
        deepLink?.let { deepLink ->
            val queryParams = deepLink.mapQueryParams()
            val observationId = queryParams["observationId"]
            if (observationId.isNullOrEmpty()
                || observationRepository.observationById(observationId.first())
                    .firstOrNull() == null
            ) {
                emit(null)
                return@flow
            }
            val schedule =
                scheduleRepository.firstScheduleAvailableForObservationId(observationId.first())
                    .cancellable().firstOrNull()
            val state = schedule?.state
            val isRead = state == NotificationStatusType.COMPLETED.toString()
            val isCompleted = state == null;
            // only not done modules have a schedule. if it has a schedule, the state will show if the element is actively collecting data
            // which means: if the state is null, the module is already completed, otherwise check if element is Deactivated to check if the message can be deselected
            when {
                isRead -> emit(NotificationStatusType.READ)
                isCompleted -> emit(NotificationStatusType.COMPLETED)
                else -> emit(NotificationStatusType.READ)
            }
        } ?: run {
            emit(NotificationStatusType.READ)
        }
    }

    fun modifyDeepLink(
        deepLink: String?,
        protocolReplacement: String? = null,
        hostReplacement: String? = null
    ): Flow<String?> = flow {
        deepLink?.let { deepLink ->
            val queryParams = deepLink.mapQueryParams()
            val observationId = queryParams["observationId"]
            if (observationId.isNullOrEmpty()
                || observationRepository.observationById(observationId.first())
                    .firstOrNull() == null
            ) {
                emit(null)
                return@flow
            }
            val schedule =
                scheduleRepository.firstScheduleAvailableForObservationId(observationId.first())
                    .cancellable().firstOrNull()

            emit(deepLinkModifier(deepLink, schedule, protocolReplacement, hostReplacement))
        } ?: run {
            emit(deepLink)
        }
    }

    private fun deepLinkModifier(
        deepLink: String,
        schedule: ScheduleSchema?,
        protocolReplacement: String?,
        hostReplacement: String?
    ): String {
        val selectedRoute = selectRoute(deepLink, schedule)
        return replaceRoute(deepLink, selectedRoute, schedule, protocolReplacement, hostReplacement)
    }

    private fun validateRoute(deepLink: String): Boolean {
        return deepLink.extractRouteFromDeepLink()?.let { route ->
            deepLinks.firstOrNull { it.contains(route) } != null
        } ?: false
    }

    private fun routeForObservation(deepLink: String): String? {
        return deepLink.extractRouteFromDeepLink()?.let { route ->
            observationFactory.observationTypes().firstOrNull {
                it.contains(route)
            }?.let {
                if (validateRoute(deepLink)) route else null
            } ?: TASK_DETAILS
        }
    }

    private fun selectRoute(deepLink: String, schedule: ScheduleSchema?): String {
        val now = Clock.System.now()

        return schedule?.let { scheduleSchema ->
            if ((scheduleSchema.start?.epochSeconds ?: 0) <= now.epochSeconds) {
                routeForObservation(deepLink)
            } else {
                TASK_DETAILS
            }
        } ?: OBSERVATION_DETAILS
    }

    private fun replaceRoute(
        deepLink: String,
        routeToReplace: String,
        schedule: ScheduleSchema? = null,
        protocolReplacement: String? = null,
        hostReplacement: String? = null
    ): String {
        val protocolAndHost = (protocolReplacement ?: deepLink.substringBefore("://")) + "://"
        val afterProtocol = deepLink.substringAfter("://")
        val hostAndPath = afterProtocol.substringBefore('?')
        val host = hostReplacement ?: hostAndPath.substringBeforeLast(
            "/",
            missingDelimiterValue = hostAndPath
        )
        val fragment = deepLink.substringAfter('#', "")

        val newHostAndPath =
            if (hostAndPath.contains('/')) "$host/$routeToReplace" else "$hostAndPath/$routeToReplace"

        val paramsMap = deepLink.mapQueryParams().toMutableMap()

        schedule?.let {
            val scheduleIdKeySet =
                paramsMap.getOrElse("scheduleId") { mutableSetOf() }.toMutableSet()
            scheduleIdKeySet.add(it.scheduleId.toHexString())
            paramsMap["scheduleId"] = scheduleIdKeySet
        }

        val newQueryParams = paramsMap.entries.flatMap { entry ->
            entry.value.map { "${entry.key}=${it}" }
        }.joinToString("&")


        return buildString {
            append(protocolAndHost)
            append(newHostAndPath)
            if (newQueryParams.isNotEmpty()) append("?").append(newQueryParams)
            if (fragment.isNotEmpty()) append("#").append(fragment)
        }
    }

    fun modifyDeepLink(
        deepLink: String?,
        protocolReplacement: String? = null,
        hostReplacement: String? = null,
        newState: (String?) -> Unit
    ) = modifyDeepLink(deepLink, protocolReplacement, hostReplacement).asClosure(newState)

    companion object {
        const val TASK_DETAILS = "task-details"
        const val OBSERVATION_DETAILS = "observation-details"
        const val DASHBOARD = "dashboard"
    }
}