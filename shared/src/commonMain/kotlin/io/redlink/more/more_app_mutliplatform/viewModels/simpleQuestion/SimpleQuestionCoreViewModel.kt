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
package io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.SimpleQuestionModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SimpleQuestionType
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull

class SimpleQuestionCoreViewModel(
    observationFactory: ObservationFactory,
): CoreViewModel() {
    private var scheduleId: String? = null
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val simpleQuestionModel = MutableStateFlow<SimpleQuestionModel?>(null)
    private var observation: Observation? = observationFactory.observation(SimpleQuestionType().observationType)

    private var notificationId: String? = null

    fun setScheduleId(scheduleId: String, notificationId: String? = null) {
        this.scheduleId = scheduleId
        this.notificationId = notificationId
        launchScope {
            scheduleRepository.scheduleWithId(scheduleId).cancellable().firstOrNull()?.let { scheduleSchema ->
                observationRepository.observationById(scheduleSchema.observationId).cancellable().firstOrNull()?.let { observationSchema ->
                    simpleQuestionModel.emit(SimpleQuestionModel.createModelFrom(observationSchema, scheduleId))
                }
            }
        }
    }

    fun setScheduleViaObservationId(observationId: String, notificationId: String? = null) {
        launchScope {
            scheduleRepository.firstScheduleIdAvailableForObservationId(observationId).cancellable().firstOrNull()?.let { setScheduleId(it, notificationId)}
        }
    }

    fun finishQuestion(data: String, setObservationToDone: Boolean){
        simpleQuestionModel.value?.let {
            observation?.let { observation ->
                observation.start(it.observationId, it.scheduleId, notificationId)
                observation.storeData(mapOf("answer" to data)) {
                    scheduleId?.let {
                        observation.stopAndSetDone(it)
                    }
                }
                notificationId = null
            }
        }
    }

    fun onLoadSimpleQuestionObservation(provideNewState: ((SimpleQuestionModel?) -> Unit)): Closeable {
        return simpleQuestionModel.asClosure(provideNewState)
    }

    override fun viewDidAppear() {

    }
}
