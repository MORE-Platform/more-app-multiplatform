package io.redlink.umm.participant.navigation

import io.github.aakira.napier.Napier
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.asClosure
import io.redlink.umm.participant.extensions.extractRouteFromDeepLink
import io.redlink.umm.participant.extensions.mapQueryParams
import io.redlink.umm.participant.observations.ObservationFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class DeeplinkManager(
    private val repos: MainRepository,
    val observationFactory: ObservationFactory
) {
    private val deepLinks = mutableSetOf<String>()

    fun addAvailableDeepLinks(deepLinks: Set<String>) {
        this.deepLinks.addAll(deepLinks)
    }

    fun modifyDeepLink(
        deepLink: String?,
        protocolReplacement: String? = null,
        hostReplacement: String? = null
    ): Flow<String?> = flow {
        deepLink?.let { deepLink ->
            val queryParams = deepLink.mapQueryParams()
            val observationIdParam = queryParams["observationId"]?.firstOrNull()
            val scheduleIdParam = queryParams["scheduleId"]?.firstOrNull()

            val schedule = scheduleIdParam?.let { id ->
                repos.schedule.scheduleWithId(id).cancellable().firstOrNull()
            } ?: observationIdParam?.let { id ->
                repos.schedule.firstScheduleAvailableForObservationId(id)
                    .cancellable().firstOrNull()
            }

            Napier.d { "Schedule: $schedule, observationId: $observationIdParam" }

            val observationIdToUse = observationIdParam ?: schedule?.observationId

            if (scheduleIdParam != null && schedule == null) {
                emit(null)
                return@flow
            }

            if (observationIdToUse.isNullOrEmpty()
                || repos.observation.observationById(observationIdToUse)
                    .firstOrNull() == null
            ) {
                emit(null)
                return@flow
            }

            if (schedule != null && observationIdParam != null && schedule.observationId != observationIdParam) {
                emit(null)
                return@flow
            }

            emit(deepLinkModifier(deepLink, schedule, protocolReplacement, hostReplacement))
        } ?: run {
            emit(deepLink)
        }
    }

    private fun deepLinkModifier(
        deepLink: String,
        schedule: ScheduleEntity?,
        protocolReplacement: String?,
        hostReplacement: String?
    ): String {
        val selectedRoute = selectRoute(deepLink, schedule)
        Napier.d { "Selected route: $selectedRoute, schedule: $schedule, observationId: ${schedule?.observationId}" }
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

    private fun selectRoute(deepLink: String, schedule: ScheduleEntity?): String {
        val now = Clock.System.now()

        return schedule?.let { scheduleSchema ->
            if ((scheduleSchema.start ?: (now.epochSeconds + 1)) <= now.epochSeconds
                && (scheduleSchema.end ?: 0) >= now.epochSeconds
            ) {
                routeForObservation(deepLink)
            } else {
                Napier.d { "Schedule is not active, using default route" }
                Napier.d { "Schedule start: ${scheduleSchema.start}, end: ${scheduleSchema.end}, currentTime: ${now.epochSeconds}" }
                TASK_DETAILS
            }
        } ?: OBSERVATION_DETAILS
    }

    private fun replaceRoute(
        deepLink: String,
        routeToReplace: String,
        schedule: ScheduleEntity? = null,
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
            scheduleIdKeySet.add(it.scheduleId)
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

    /**
     * Creates a deep link for a given [ScheduleEntity].
     *
     * Expected format:
     *   <baseDeeplink>/<route>?observationId=<id>&scheduleId=<id>
     *
     * `baseDeeplink` should look like: "<scheme>://<host>/" (including the trailing slash).
     */
    fun createDeeplinkForSchedule(
        schedule: ScheduleEntity,
        baseDeeplink: String? = null
    ): String {
        val host = baseDeeplink ?: BASE_HOST
        val base = if (host.endsWith("/")) host else "$host/"

        val observationRoute = observationFactory.observationTypes().firstOrNull {
            (it == schedule.observationType || it.contains(schedule.observationType))
        } ?: TASK_DETAILS

        val finalRoute = if (deepLinks.any { it.contains(observationRoute) }) {
            observationRoute
        } else {
            TASK_DETAILS
        }

        return buildString {
            append(base)
            append(finalRoute)
            append("?observationId=")
            append(schedule.observationId)
            append("&scheduleId=")
            append(schedule.scheduleId)
        }
    }


    companion object {
        const val TASK_DETAILS = "task-details"
        const val OBSERVATION_DETAILS = "observation-details"
        private const val BASE_HOST = "app://io.redlink.umm.blendedcare/"
    }
}