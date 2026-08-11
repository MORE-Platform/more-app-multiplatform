package io.redlink.more.services.network.demo

import io.redlink.more.services.network.openapi.model.ApiKey
import io.redlink.more.services.network.openapi.model.AppConfiguration
import io.redlink.more.services.network.openapi.model.ContactInfo
import io.redlink.more.services.network.openapi.model.Observation
import io.redlink.more.services.network.openapi.model.ObservationSchedule
import io.redlink.more.services.network.openapi.model.PushNotification
import io.redlink.more.services.network.openapi.model.SimpleParticipant
import io.redlink.more.services.network.openapi.model.Study
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

object DemoData {
    fun createDemoStudy(): Study {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val observationStart = now.minus(1, DateTimeUnit.HOUR)
        val observationEnd = now.plus(1, DateTimeUnit.HOUR)

        return Study(
            studyTitle = "Demo Study",
            participantInfo = "This is a demo study for testing purposes.",
            consentInfo = "By using this demo, you agree to the mock terms and conditions.",
            start = today.minus(1, DateTimeUnit.DAY),
            end = today.plus(30, DateTimeUnit.DAY),
            version = now.toEpochMilliseconds(),
            active = true,
            studyState = Study.StudyState.ACTIVE,
            observations = listOf(
//                Observation(
//                    observationId = "1",
//                    observationType = "question-observation",
//                    observationTitle = "Daily Mood",
//                    participantInfo = "Please rate your mood today.",
//                    configuration = buildJsonObject {
//                        put("question", "How are you feeling today?")
//                        putJsonArray("answers") {
//                            add(JsonPrimitive("Great"))
//                            add(JsonPrimitive("Good"))
//                            add(JsonPrimitive("Okay"))
//                            add(JsonPrimitive("Bad"))
//                            add(JsonPrimitive("Very Bad"))
//                        }
//                    },
//                    schedule = listOf(
//                        ObservationSchedule(
//                            start = observationStart,
//                            end = observationEnd
//                        )
//                    ),
//                    required = true,
//                    version = now.toEpochMilliseconds()
//                ),
//                Observation(
//                    observationId = "2",
//                    observationType = "multiple-choice-question-observation",
//                    observationTitle = "Symptoms",
//                    participantInfo = "Please select all symptoms you experienced today.",
//                    configuration = buildJsonObject {
//                        put("question", "Which symptoms did you have today?")
//                        putJsonArray("answers") {
//                            add(JsonPrimitive("Headache"))
//                            add(JsonPrimitive("Cough"))
//                            add(JsonPrimitive("Fever"))
//                            add(JsonPrimitive("Nausea"))
//                            add(JsonPrimitive("Fatigue"))
//                        }
//                    },
//                    schedule = listOf(
//                        ObservationSchedule(
//                            start = observationStart,
//                            end = observationEnd
//                        )
//                    ),
//                    required = false,
//                    version = now.toEpochMilliseconds()
//                ),
//                Observation(
//                    observationId = "3",
//                    observationType = "lime-survey-observation",
//                    observationTitle = "Health Questionnaire",
//                    participantInfo = "A more detailed health questionnaire.",
//                    schedule = listOf(
//                        ObservationSchedule(
//                            start = observationStart,
//                            end = observationEnd
//                        )
//                    ),
//                    required = false,
//                    version = now.toEpochMilliseconds()
//                ),
                Observation(
                    observationId = "4",
                    observationType = "app-usage-observation",
                    observationTitle = "App Usage",
                    participantInfo = "Monitoring app usage for study purposes.",
                    noSchedule = true,
                    schedule = emptyList(),
                    required = false,
                    version = now.toEpochMilliseconds(),
                    hidden = true
                ),
//                Observation(
//                    observationId = "5",
//                    observationType = "acc-mobile-observation",
//                    observationTitle = "Activity Tracking",
//                    participantInfo = "Using the accelerometer to track activity.",
//                    schedule = listOf(
//                        ObservationSchedule(
//                            start = now.minus(24, DateTimeUnit.HOUR),
//                            end = now.plus(30, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
//                        )
//                    ),
//                    required = false,
//                    version = now.toEpochMilliseconds()
//                ),
//                Observation(
//                    observationId = "6",
//                    observationType = "gps-mobile-observation",
//                    observationTitle = "Location Tracking",
//                    participantInfo = "Tracking location for study purposes.",
//                    schedule = listOf(
//                        ObservationSchedule(
//                            start = now.minus(24, DateTimeUnit.HOUR),
//                            end = now.plus(30, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
//                        )
//                    ),
//                    required = false,
//                    version = now.toEpochMilliseconds()
//                ),
//                Observation(
//                    observationId = "7",
//                    observationType = "polar-verity-observation",
//                    observationTitle = "Heart Rate",
//                    participantInfo = "Heart rate monitoring via Polar sensor.",
//                    schedule = listOf(
//                        ObservationSchedule(
//                            start = now.minus(24, DateTimeUnit.HOUR),
//                            end = now.plus(30, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
//                        )
//                    ),
//                    required = false,
//                    version = now.toEpochMilliseconds()
//                )
                // hidden observation
                Observation(
                    observationId = "1",
                    observationType = "question-observation",
                    observationTitle = "Daily Mood",
                    participantInfo = "Please rate your mood today.",
                    configuration = buildJsonObject {
                        put("question", "How are you feeling today?")
                        putJsonArray("answers") {
                            add(JsonPrimitive("Great"))
                            add(JsonPrimitive("Good"))
                            add(JsonPrimitive("Okay"))
                            add(JsonPrimitive("Bad"))
                            add(JsonPrimitive("Very Bad"))
                        }
                    },
                    schedule = listOf(
                        ObservationSchedule(
                            start = observationStart,
                            end = observationEnd
                        )
                    ),
                    hidden = true,
                    required = true,
                    reminder = true,
                    version = now.toEpochMilliseconds()
                ),
                // Boolean Goal Observation
            ),
            contact = ContactInfo(
                person = "Demo Support",
                email = "support@demo.more-platform.org",
                institute = "Demo Institute of Health Research",
                phoneNumber = "+1234567890"
            ),
            participant = SimpleParticipant(
                id = 1,
                alias = "Demo Participant",
            )
        )
    }


    fun getDemoNotifications(): List<PushNotification> {
        val now = Clock.System.now()
        return listOf(
            PushNotification(
                type = PushNotification.Type.TEXT,
                msgId = "demo_1",
                title = "Welcome to the Study!",
                body = "We are glad you are here. This is a demo notification.",
                timestamp = now.plus(5, DateTimeUnit.MINUTE)
            ),
            PushNotification(
                type = PushNotification.Type.TEXT,
                msgId = "demo_2",
                title = "Daily Mood Check",
                body = "Please complete your mood check for today.",
                timestamp = now.plus(10, DateTimeUnit.MINUTE),
                deepLink = "more://task-details?observationId=1"
            ),
            PushNotification(
                type = PushNotification.Type.TEXT,
                msgId = "demo_3",
                title = "New Observation available",
                body = "A new questionnaire is waiting for you.",
                timestamp = now.plus(15, DateTimeUnit.MINUTE),
                deepLink = "more://task-details?observationId=2"
            ),
            PushNotification(
                type = PushNotification.Type.TEXT,
                msgId = "demo_general_1",
                title = "Allgemeine Info",
                body = "Dies ist eine wichtige Nachricht ohne direkte Aufgabe.",
                timestamp = now.minus(2, DateTimeUnit.MINUTE)
            ),
            // Observation Reminder
            PushNotification(
                type = PushNotification.Type.TEXT,
                msgId = "reminder_observation",
                title = "Wie geht es dir?",
                body = "Bitte fülle deinen täglichen Mood-Check aus.",
                timestamp = now.minus(5, DateTimeUnit.MINUTE),
                deepLink = "more://task-details?observationId=1&scheduleId=observation-schedule-1",
                data = buildJsonObject {
                    put("observationId", "1")
                    put("scheduleId", "observation-schedule-1")
                    put(
                        "observationType",
                        io.redlink.more.observations.observationTypes.QuestionType().observationType
                    )
                    put("reminderType", "reminder")
                }
            ),
        )
    }

    fun getDemoSchedules(): List<ObservationSchedule> {
        val now = Clock.System.now()
        val observationStart = now.minus(1, DateTimeUnit.HOUR)
        val observationEnd = now.plus(1, DateTimeUnit.HOUR)
        return listOf(
            ObservationSchedule(
                start = observationStart,
                end = observationEnd
            )
        )
    }

    fun getDemoAppConfiguration(baseUrl: String): AppConfiguration {
        return AppConfiguration(
            credentials = ApiKey(apiId = "DEMO_ID", apiKey = "DEMO_KEY"),
            endpoint = baseUrl
        )
    }
}
