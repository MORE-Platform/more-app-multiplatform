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

package io.redlink.more.navigation.model

import io.redlink.more.observations.observationTypes.GarminType
import io.redlink.more.observations.observationTypes.LimeSurveyType
import io.redlink.more.observations.observationTypes.QuestionType

enum class NavigationRoute(val route: String) {
    DASHBOARD("dashboard"),
    NOTIFICATIONS("notifications"),
    INFO("information"),
    SETTINGS("settings"),
    SCHEDULE_DETAILS("task-details"),
    OBSERVATION_DETAILS("observation-details"),
    STUDY_DETAILS("study-details"),
    OBSERVATION_FILTER("observation-filter"),
    QUESTION(QuestionType().observationType),
    QUESTIONNAIRE_RESPONSE("${QuestionType().observationType}_response"),
    BLUETOOTH_CONNECTION("devices"),
    RUNNING_SCHEDULES("running-observations"),
    COMPLETED_SCHEDULES("past-observations"),
    NOTIFICATION_FILTER("notification-filter"),
    LEAVE_STUDY("leave-study"),
    LEAVE_STUDY_CONFIRM("leave-study-confirmation"),
    LIMESURVEY(LimeSurveyType().observationType),
    GARMIN_CONNECT(GarminType().observationType),
    OBSERVATION_ERRORS("observation-errors"),
    QR_CODE("scan-qr-code");
}