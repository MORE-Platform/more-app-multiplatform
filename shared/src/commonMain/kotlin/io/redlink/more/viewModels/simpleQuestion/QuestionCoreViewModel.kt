/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.viewModels.simpleQuestion

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.logging.event
import io.redlink.more.models.QuestionModel
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.observations.Observation
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.appUsage.model.LogEvent
import io.redlink.more.observations.observationTypes.QuestionType
import io.redlink.more.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

class QuestionCoreViewModel(
    private val repository: MainRepository,
    observationFactory: ObservationFactory,
    private var scheduleId: String? = null,
    private val notificationId: String? = null,
    private val observationId: String? = null
) : CoreViewModel() {
    private val _questionModel = MutableStateFlow<QuestionModel?>(null)

    @NativeCoroutines
    val questionModel: StateFlow<QuestionModel?> = _questionModel
    private var observation: Observation? =
        observationFactory.observation(QuestionType().observationType)

    init {
        launchScope {
            if (scheduleId == null && observationId != null) {
                scheduleId = repository
                    .schedule
                    .firstScheduleIdAvailableForObservationId(observationId)
                    .cancellable()
                    .firstOrNull()
            }
            scheduleId?.let { scheduleId ->
                repository.schedule.scheduleWithId(scheduleId).cancellable().firstOrNull()
                    ?.let { scheduleSchema ->
                        repository.observation.observationById(scheduleSchema.observationId)
                            .cancellable().firstOrNull()?.let { observationSchema ->
                                _questionModel.update {
                                    QuestionModel.createModelFrom(
                                        observationSchema,
                                        scheduleId
                                    )
                                }
                            }
                    }
            }
        }
    }

    fun finishQuestion(data: Any) {
        _questionModel.value?.let { questionModel ->
            Napier.event(
                LogEvent.OBSERVATION_EVENT,
                "Questionnaire answered, but not yet sent, for Observation ID: $observationId"
            )
            observation?.let { observation ->
                observation.start(
                    questionModel.observationId,
                    questionModel.scheduleId,
                    notificationId
                )
                observation.storeData(mapOf(questionModel.type.observationDataResponseKey to data)) {
                    Napier.event(
                        LogEvent.OBSERVATION_EVENT,
                        "Questionnaire answer successfully sent with Observation ID: $observationId"
                    )
                    scheduleId?.let {
                        observation.stopAndSetDone(it)
                    }
                }
            }
        }
    }

    override fun viewIdentifier(): String {
        return NavigationRoute.QUESTION.viewIdentifier
    }
}
