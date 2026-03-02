package io.redlink.more.navigation

import io.github.aakira.napier.Napier
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.asClosure
import io.redlink.more.extensions.extractRouteFromDeepLink
import io.redlink.more.extensions.mapQueryParams
import io.redlink.more.navigation.model.DeepLinkData
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.navigation.model.NavigationRouteParameter
import io.redlink.more.observations.ObservationFactory
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
    private var protocolReplacement: String? = null
    private var hostReplacement: String? = null

    fun addAvailableDeepLinks(deepLinks: Set<String>) {
        this.deepLinks.addAll(deepLinks)
    }

    fun setProtocol(protocolReplacement: String?) {
        this.protocolReplacement = protocolReplacement
    }

    fun setHost(hostReplacement: String?) {
        this.hostReplacement = hostReplacement
    }

    fun getNotificationViewDeepLink(
        notificationId: String,
    ): Flow<DeepLinkData?> {
        return modifyDeepLink(
            "/${NavigationRoute.NOTIFICATIONS}?${NavigationRouteParameter.NOTIFICATION_ID}=$notificationId",
        )
    }

    fun modifyDeepLink(
        deepLink: String?,
    ): Flow<DeepLinkData?> = flow {
        deepLink?.let { deepLink ->
            val queryParams = deepLink.mapQueryParams()
            val observationIdParam =
                queryParams[NavigationRouteParameter.OBSERVATION_ID.key]?.firstOrNull()
            val scheduleIdParam =
                queryParams[NavigationRouteParameter.SCHEDULE_ID.key]?.firstOrNull()
            val notificationId =
                queryParams[NavigationRouteParameter.NOTIFICATION_ID.key]?.firstOrNull()

            val schedule = scheduleIdParam?.let { id ->
                repos.schedule.scheduleWithId(id).cancellable().firstOrNull()
            } ?: observationIdParam?.let { id ->
                repos.schedule.firstScheduleAvailableForObservationId(id)
                    .cancellable().firstOrNull()
            }

            Napier.d { "Schedule: $schedule, observationId: $observationIdParam" }

            val observationIdToUse = observationIdParam ?: schedule?.observationId

            emit(
                DeepLinkData(
                    deepLinkModifier(
                        deepLink,
                        schedule,
                    ),
                    mapOf(
                        NavigationRouteParameter.NOTIFICATION_ID.key to notificationId,
                        NavigationRouteParameter.SCHEDULE_ID.key to scheduleIdParam,
                        NavigationRouteParameter.OBSERVATION_ID.key to observationIdToUse
                    )
                )
            )
        } ?: run {
            emit(deepLink?.let { DeepLinkData(it) })
        }
    }

    private fun deepLinkModifier(
        deepLink: String,
        schedule: ScheduleEntity?,
    ): String {
        val selectedRoute = selectRoute(deepLink, schedule)
        Napier.d { "Selected route: $selectedRoute, schedule: $schedule, observationId: ${schedule?.observationId}" }
        return replaceRoute(deepLink, selectedRoute, schedule)
    }


    /**
     * Returns true if [incomingRoute] should be treated as the same logical route as [registeredRoute].
     *
     * This allows deeplinks like `question-observation_response` to resolve to the registered
     * navigation route `question-observation` (or other variants where the incoming route has a
     * suffix/prefix separated by '_' or '-').
     */
    private fun routeMatches(incomingRoute: String, registeredRoute: String): Boolean {
        if (incomingRoute == registeredRoute) return true

        fun startsWithDelimited(value: String, prefix: String): Boolean {
            if (!value.startsWith(prefix)) return false
            if (value.length == prefix.length) return true
            return value[prefix.length] == '_' || value[prefix.length] == '-'
        }

        return startsWithDelimited(incomingRoute, registeredRoute) ||
                startsWithDelimited(registeredRoute, incomingRoute)
    }

    /** Extracts the route part from a registered deep link uriPattern string. */
    private fun extractRegisteredRoute(uriPattern: String): String? =
        uriPattern.extractRouteFromDeepLink()

    private fun validateRoute(deepLink: String): Boolean {
        Napier.d { "Available deeplinks: $deepLinks" }
        return deepLinks.any { registered ->
            val registeredRoute = extractRegisteredRoute(registered) ?: registered
            routeMatches(deepLink, registeredRoute)
        }
    }

    private fun routeForObservation(deepLink: String): String {
        val incomingRoute = extractIncomingRoute(deepLink.lowercase())
            ?: return NavigationRoute.SCHEDULE_DETAILS.route

        val resolvedObservationRoute =
            observationFactory.getMatchingObservationTypes(setOf(incomingRoute)).firstOrNull()
                ?: incomingRoute

        Napier.d { "Resolved observation route: $resolvedObservationRoute" }

        val valid = validateRoute(resolvedObservationRoute)
        Napier.d { "Validating route: $valid" }
        return if (valid) {
            resolvedObservationRoute
        } else NavigationRoute.DASHBOARD.route
    }

    private fun extractIncomingRoute(raw: String): String? {
        // 1) Try the existing extractor (works for full deeplinks like scheme://host/path?...)
        val extracted = raw.extractRouteFromDeepLink()
        if (!extracted.isNullOrBlank()) return extracted

        // 2) Fallback: treat the input as a route/path-only string
        //    e.g. "/notifications", "notifications", "notifications?x=1", "/foo#bar"
        return raw.trim()
            .removePrefix("/")
            .substringBefore('?')
            .substringBefore('#')
            .takeIf { it.isNotBlank() }
    }

    private fun selectRoute(deepLink: String, schedule: ScheduleEntity?): String {
        val now = Clock.System.now()

        return schedule?.let { scheduleSchema ->
            if ((scheduleSchema.start ?: (now.epochSeconds + 1)) <= now.epochSeconds
                && (scheduleSchema.end ?: 0) >= now.epochSeconds
                && scheduleSchema.getState().active()
            ) {
                routeForObservation(deepLink)
            } else {
                Napier.d { "Schedule is not active, using default route" }
                Napier.d { "Schedule start: ${scheduleSchema.start}, end: ${scheduleSchema.end}, currentTime: ${now.epochSeconds}" }
                NavigationRoute.SCHEDULE_DETAILS.route
            }
        } ?: routeForObservation(deepLink)
    }

    private fun replaceRoute(
        deepLink: String,
        routeToReplace: String,
        schedule: ScheduleEntity? = null,
    ): String {
        val protocolAndHost = (this.protocolReplacement
            ?: deepLink.substringBefore("://")) + "://"
        val afterProtocol = deepLink.substringAfter("://")
        val hostAndPath = afterProtocol.substringBefore('?')
        val host = this.hostReplacement ?: hostAndPath.substringBeforeLast(
            "/",
            missingDelimiterValue = hostAndPath
        )
        val fragment = deepLink.substringAfter('#', "")

        val newHostAndPath =
            if (hostAndPath.contains('/')) "$host/$routeToReplace" else "$hostAndPath/$routeToReplace"

        val paramsMap = deepLink.mapQueryParams().toMutableMap()

        schedule?.let {
            val scheduleIdKeySet =
                paramsMap.getOrElse(NavigationRouteParameter.SCHEDULE_ID.key) { mutableSetOf() }
                    .toMutableSet()
            scheduleIdKeySet.add(it.scheduleId)
            paramsMap[NavigationRouteParameter.SCHEDULE_ID.key] = scheduleIdKeySet
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
        newState: (DeepLinkData?) -> Unit
    ) = modifyDeepLink(deepLink).asClosure(newState)

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
        val host = baseDeeplink ?: "$protocolReplacement://$hostReplacement/"
        val base = if (host.endsWith("/")) host else "$host/"

        val observationRoute =
            observationFactory.getMatchingObservationTypes(setOf(schedule.observationType))
                .firstOrNull() ?: NavigationRoute.SCHEDULE_DETAILS.route

        return buildString {
            append(base)
            append(observationRoute)
            append("?observationId=")
            append(schedule.observationId)
            append("&scheduleId=")
            append(schedule.scheduleId)
        }
    }
}