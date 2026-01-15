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
package io.redlink.umm.participant.viewModels.simpleQuestion

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.ktor.utils.io.core.Closeable
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.asClosure
import io.redlink.umm.participant.models.SimpleQuestionModel
import io.redlink.umm.participant.observations.Observation
import io.redlink.umm.participant.observations.ObservationFactory
import io.redlink.umm.participant.observations.observationTypes.SimpleQuestionType
import io.redlink.umm.participant.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

class SimpleQuestionCoreViewModel(
    private val repository: MainRepository,
    observationFactory: ObservationFactory,
    private var scheduleId: String? = null,
    private val notificationId: String? = null,
    private val observationId: String? = null
) : CoreViewModel() {
    private val _simpleQuestionModel = MutableStateFlow<SimpleQuestionModel?>(null)

    @NativeCoroutines
    val simpleQuestionModel: StateFlow<SimpleQuestionModel?> = _simpleQuestionModel
    private var observation: Observation? =
        observationFactory.observation(SimpleQuestionType().observationType)

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
                                _simpleQuestionModel.update {
                                    SimpleQuestionModel.createModelFrom(
                                        observationSchema,
                                        scheduleId
                                    )
                                }
                            }
                    }
            }
        }
    }

    fun finishQuestion(data: String, setObservationToDone: Boolean) {
        _simpleQuestionModel.value?.let {
            observation?.let { observation ->
                observation.start(it.observationId, it.scheduleId, notificationId)
                observation.storeData(mapOf("answer" to data)) {
                    scheduleId?.let {
                        observation.stopAndSetDone(it)
                    }
                }
            }
        }
    }
}
