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
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.LimeSurveyType
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SimpleQuestionType
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SelfLearningMultipleChoiceQuestionObservationType

data class NavigationParameter(
    val type: NavType<*>,
    val default: Any?,
    val nullable: Boolean = true
)

enum class NavigationScreen(
    private val route: String,
    val parameters: Map<String, NavigationParameter> = emptyMap(),
    @StringRes val stringResource: Int
) {
    DASHBOARD("dashboard", stringResource = R.string.nav_dashboard),
    NOTIFICATIONS("notifications", stringResource = R.string.nav_notifications),
    INFO("information", stringResource = R.string.nav_info),
    SETTINGS("settings", stringResource = R.string.nav_settings),
    SCHEDULE_DETAILS(
        "task_details", parameters = mapOf(
            "scheduleId" to NavigationParameter(type = NavType.StringType, ""),
            "scheduleListType" to NavigationParameter(type = NavType.StringType, "")
        ), stringResource = R.string.nav_task_detail
    ),
    OBSERVATION_DETAILS(
        "observation_details",
        mapOf("observationId" to NavigationParameter(type = NavType.StringType, "")),
        stringResource = R.string.nav_observation_detail
    ),
    STUDY_DETAILS("study_details", stringResource = R.string.nav_study_details),
    OBSERVATION_FILTER(
        "observation_filter", mapOf(
            "scheduleListType" to NavigationParameter(type = NavType.StringType, "")
        ), stringResource = R.string.nav_observation_filter
    ),
    SIMPLE_QUESTION(
        SimpleQuestionType().observationType,
        parameters = mapOf(
            "scheduleId" to NavigationParameter(type = NavType.StringType, ""),
            "observationId" to NavigationParameter(type = NavType.StringType, "")
        ), stringResource = R.string.nav_simple_question
    ),
    QUESTIONNAIRE_RESPONSE(
        "${SimpleQuestionType().observationType}_response",
        stringResource = R.string.nav_simple_question
    ),
    BLUETOOTH_CONNECTION("devices", stringResource = R.string.more_ble_view_title),
    RUNNING_SCHEDULES("running_observations", stringResource = R.string.nav_running_schedules),
    COMPLETED_SCHEDULES("past_observations", stringResource = R.string.nav_completed_schedules),
    NOTIFICATION_FILTER("notification_filter", stringResource = R.string.nav_notification_filter),
    LEAVE_STUDY("leave_study", stringResource = R.string.nav_leave_study),
    LEAVE_STUDY_CONFIRM(
        "leave_study_confirmation",
        stringResource = R.string.nav_leave_study_confirm
    ),
    LIMESURVEY(
        LimeSurveyType().observationType, mapOf(
            "scheduleId" to NavigationParameter(NavType.StringType, ""),
            "observationId" to NavigationParameter(
                NavType.StringType, ""
            )
        ), stringResource = R.string.nav_limesurvey
    ),    
    SELF_LEARNING_MULTIPLE_CHOICE_QUESTION(
        SelfLearningMultipleChoiceQuestionObservationType().observationType,
        parameters = mapOf(
            "scheduleId" to NavigationParameter(type = NavType.StringType, ""),
            "observationId" to NavigationParameter(type = NavType.StringType, "")
        ), stringResource = R.string.nav_self_learning_multiple_choice_question
    ),
    SELF_LEARNING_MULTIPLE_CHOICE_QUESTION_RESPONSE(
        "${SelfLearningMultipleChoiceQuestionObservationType().observationType}_response",
        stringResource = R.string.nav_self_learning_multiple_choice_question
    );

    private var cachedNavArguments: List<NamedNavArgument>? = null
    private var cachedRoute: String? = null
    private var cachedDeepLinks: List<NavDeepLink>? = null

    @Composable
    fun stringRes() = getStringResource(id = stringResource)

    private fun allParam() = parameters + globalParameters

    fun routeWithParameters(): String {
        if (cachedRoute == null) {
            var fullRoute = route
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
        var fullRoute = route
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
            queryParams.add("$NavigationNotificationIDKey=$it")
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

    fun createDeepLinkRoute(deepLinkHost: String): List<NavDeepLink> {
        if (cachedDeepLinks == null) {
            cachedDeepLinks =
                listOf(navDeepLink { uriPattern = deepLinkHost + routeWithParameters() })
        }
        return cachedDeepLinks!!
    }

    companion object {
        const val NavigationNotificationIDKey = "notificationId"

        private val globalParameters =
            mapOf(NavigationNotificationIDKey to NavigationParameter(NavType.StringType, ""))

        fun byRoute(route: String) = NavigationScreen.values().firstOrNull { it.route == route }
    }
}