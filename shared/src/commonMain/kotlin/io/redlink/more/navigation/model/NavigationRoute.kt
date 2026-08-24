/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.navigation.model

import io.redlink.more.observations.observationTypes.GarminType
import io.redlink.more.observations.observationTypes.LimeSurveyType
import io.redlink.more.observations.observationTypes.QuestionType

enum class NavigationRoute(val route: String, val viewIdentifier: String) {
    LOGIN("login", "Login"),
    CONSENT("consent", "Consent"),
    DASHBOARD("dashboard", "Dashboard/Manual tasks"),
    NOTIFICATIONS("notifications", "Notifications"),
    INFO("information", "Information Menu"),
    SETTINGS("settings", "Settings"),
    SCHEDULE_DETAILS("task-details", "Task Detail Information"),
    OBSERVATION_DETAILS("observation-details", "Observation Detail Information"),
    STUDY_DETAILS("study-details", "Study Detail Information"),
    OBSERVATION_FILTER("observation-filter", "Observation Filter"),
    QUESTION(
        QuestionType().observationType,
        "${QuestionType().observationType} Questionnaire Interaction"
    ),
    QUESTIONNAIRE_RESPONSE(
        "${QuestionType().observationType}_response",
        "${QuestionType().observationType} Questionnaire Response"
    ),
    BLUETOOTH_CONNECTION("devices", "Bluetooth Connections"),
    RUNNING_SCHEDULES("running-observations", "Running Observation List"),
    COMPLETED_SCHEDULES("past-observations", "Completed Observation List"),
    NOTIFICATION_FILTER("notification-filter", "Notification Filter"),
    LEAVE_STUDY("leave-study", "Study Exit"),
    LEAVE_STUDY_CONFIRM("leave-study-confirmation", "Study Exit Confirmation"),
    LIMESURVEY(LimeSurveyType().observationType, "LimeSurvey Interaction"),
    GARMIN_CONNECT(GarminType().observationType, "Garmin Connect Login"),
    OBSERVATION_ERRORS("observation-errors", "Observation Errors"),
    QR_CODE("scan-qr-code", "Scan QR Code");
}