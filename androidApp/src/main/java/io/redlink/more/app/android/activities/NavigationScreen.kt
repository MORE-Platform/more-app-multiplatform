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
package io.redlink.more.app.android.activities

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.navigation.model.NavigationRouteParameter

data class NavigationParameter(
    val type: NavType<*>,
    val default: Any?,
    val nullable: Boolean = true
)

enum class NavigationScreen(
    private val route: NavigationRoute,
    val parameters: Map<String, NavigationParameter> = emptyMap(),
    @StringRes val stringResource: Int
) {
    DASHBOARD(NavigationRoute.DASHBOARD, stringResource = R.string.nav_dashboard),
    NOTIFICATIONS(NavigationRoute.NOTIFICATIONS, stringResource = R.string.nav_notifications),
    INFO(NavigationRoute.INFO, stringResource = R.string.nav_info),
    SETTINGS(NavigationRoute.SETTINGS, stringResource = R.string.nav_settings),
    SCHEDULE_DETAILS(
        NavigationRoute.SCHEDULE_DETAILS, parameters = mapOf(
            NavigationRouteParameter.SCHEDULE_ID.key to NavigationParameter(
                type = NavType.StringType,
                ""
            )
        ), stringResource = R.string.nav_task_detail
    ),
    OBSERVATION_DETAILS(
        NavigationRoute.OBSERVATION_DETAILS,
        mapOf(
            NavigationRouteParameter.OBSERVATION_ID.key to NavigationParameter(
                type = NavType.StringType,
                ""
            )
        ),
        stringResource = R.string.nav_observation_detail
    ),
    STUDY_DETAILS(NavigationRoute.STUDY_DETAILS, stringResource = R.string.nav_study_details),
    OBSERVATION_FILTER(
        NavigationRoute.OBSERVATION_FILTER, mapOf(
            NavigationRouteParameter.SCHEDULE_LIST_TYPE.key to NavigationParameter(
                type = NavType.StringType,
                ""
            )
        ), stringResource = R.string.nav_observation_filter
    ),
    QUESTION(
        NavigationRoute.QUESTION,
        parameters = mapOf(
            NavigationRouteParameter.SCHEDULE_ID.key to NavigationParameter(
                type = NavType.StringType,
                ""
            ),
            NavigationRouteParameter.OBSERVATION_ID.key to NavigationParameter(
                type = NavType.StringType,
                ""
            )
        ), stringResource = R.string.nav_question
    ),
    QUESTIONNAIRE_RESPONSE(
        NavigationRoute.QUESTIONNAIRE_RESPONSE,
        stringResource = R.string.nav_question
    ),
    BLUETOOTH_CONNECTION(
        NavigationRoute.BLUETOOTH_CONNECTION,
        stringResource = R.string.more_ble_view_title
    ),
    RUNNING_SCHEDULES(
        NavigationRoute.RUNNING_SCHEDULES,
        stringResource = R.string.nav_running_schedules
    ),
    COMPLETED_SCHEDULES(
        NavigationRoute.COMPLETED_SCHEDULES,
        stringResource = R.string.nav_completed_schedules
    ),
    NOTIFICATION_FILTER(
        NavigationRoute.NOTIFICATION_FILTER,
        stringResource = R.string.nav_notification_filter
    ),
    LEAVE_STUDY(NavigationRoute.LEAVE_STUDY, stringResource = R.string.nav_leave_study),
    LEAVE_STUDY_CONFIRM(
        NavigationRoute.LEAVE_STUDY_CONFIRM,
        stringResource = R.string.nav_leave_study_confirm
    ),
    LIMESURVEY(
        NavigationRoute.LIMESURVEY, mapOf(
            NavigationRouteParameter.SCHEDULE_ID.key to NavigationParameter(NavType.StringType, ""),
            NavigationRouteParameter.OBSERVATION_ID.key to NavigationParameter(
                NavType.StringType, ""
            )
        ), stringResource = R.string.nav_limesurvey
    ),
    GARMIN_CONNECT(
        NavigationRoute.GARMIN_CONNECT,
        parameters = mapOf(),
        stringResource = R.string.nav_garmin_connect
    ),

    OBSERVATION_ERRORS(
        NavigationRoute.OBSERVATION_ERRORS,
        stringResource = R.string.nav_observation_errors
    );

    private var cachedNavArguments: List<NamedNavArgument>? = null
    private var cachedRoute: String? = null
    private var cachedDeepLinks: List<NavDeepLink>? = null

    @Composable
    fun stringRes() = getStringResource(id = stringResource)

    private fun allParam() = parameters + globalParameters

    fun routeWithParameters(): String {
        if (cachedRoute == null) {
            var fullRoute = route.route
            val params = allParam()
            if (params.isNotEmpty()) {
                fullRoute += "?"
                params.entries.forEachIndexed { index, entry ->
                    fullRoute += "${entry.key}={${entry.key}}"
                    if (index < params.size - 1) {
                        fullRoute += "&"
                    }
                }
            }
            cachedRoute = fullRoute
        }
        return cachedRoute!!
    }

    fun navigationRoute(
        vararg routeParameters: Pair<String, Any?>,
        notificationId: String? = null
    ): String {
        var fullRoute = route.route
        val routeMap = routeParameters.toMap()
        val params = allParam()
        val queryParams = mutableListOf<String>()

        if (params.isNotEmpty() && routeMap.isNotEmpty()) {
            params.entries.forEach { entry ->
                if (entry.key in routeMap.keys) {
                    queryParams.add("${entry.key}=${routeMap[entry.key]}")
                }
            }
        }

        notificationId?.let {
            queryParams.add("${NavigationRouteParameter.NOTIFICATION_ID.key}=$it")
        }

        if (queryParams.isNotEmpty()) {
            fullRoute += "?" + queryParams.joinToString("&")
        }

        return fullRoute
    }

    fun createListOfNavArguments(): List<NamedNavArgument> {
        if (cachedNavArguments == null) {
            cachedNavArguments = allParam().map {
                navArgument(it.key) {
                    type = it.value.type
                    defaultValue = it.value.default
                    nullable = it.value.nullable
                }
            }
        }
        return cachedNavArguments!!
    }

    fun createDeepLinkRoute(deepLinkHost: String = ContentActivity.DEEPLINK): List<NavDeepLink> {
        if (cachedDeepLinks == null) {
            cachedDeepLinks =
                listOf(navDeepLink { uriPattern = deepLinkHost + routeWithParameters() })
            cachedDeepLinks?.let { list ->
                MoreApplication.shared!!.deeplinkManager.addAvailableDeepLinks(list.mapNotNull { it.uriPattern }
                    .toSet())
            }
        }
        return cachedDeepLinks!!
    }

    companion object {
        private val globalParameters =
            mapOf(
                NavigationRouteParameter.NOTIFICATION_ID.key to NavigationParameter(
                    NavType.StringType,
                    ""
                )
            )

        fun byRoute(route: String) = entries.firstOrNull { it.route.route == route }

        fun allDeepLinks(deepLinkHost: String) =
            entries.flatMap { it.createDeepLinkRoute(deepLinkHost).mapNotNull { it.uriPattern } }
                .toSet()

        fun createDeepLinksForAllRoutes() {
            entries.forEach { it.createDeepLinkRoute() }
        }
    }
}