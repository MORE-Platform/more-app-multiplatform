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
package io.redlink.more.viewModels.limeSurvey

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.set
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoreLimeSurveyViewModel(
    private val repositories: MainRepository,
    observationFactory: ObservationFactory,
    private var scheduleId: String? = null,
    notificationId: String? = null,
    private val observationId: String? = null
) :
    CoreViewModel() {
    private var observation: LimeSurveyObservation =
        observationFactory.observation("lime-survey-observation") as? LimeSurveyObservation
            ?: throw IllegalStateException("No lime-survey-observation found in ObservationFactory")

    @NativeCoroutines
    val limeSurveyLink: StateFlow<String?> = observation.limeURL
    private val _dataLoading = MutableStateFlow(false)

    @NativeCoroutines
    val dataLoading: StateFlow<Boolean> = _dataLoading

    init {
        viewModelScope.launch {
            if (scheduleId == null && observationId != null) {
                scheduleId =
                    repositories.schedule.firstScheduleIdAvailableForObservationId(observationId)
                        .cancellable()
                        .firstOrNull()
            }

            scheduleId?.let { scheduleId ->
                Napier.i { "Setting scheduleId: $scheduleId for LimeSurvey" }
                if (scheduleId.isNotEmpty() || scheduleId.isNotBlank()) {
                    _dataLoading.update { true }
                    repositories.schedule.scheduleWithId(scheduleId).cancellable()
                        .transform { scheduleSchema ->
                            emit(scheduleSchema?.let {
                                repositories.observation.observationById(it.observationId)
                                    .cancellable().firstOrNull()
                            })
                        }.cancellable().firstOrNull().let { observationSchema ->
                            observationSchema?.let {
                                observation.observationConfig(it.configAsMap())
                                observation.start(it.observationId, scheduleId, notificationId)
                            }
                            _dataLoading.set(false)
                        }
                }
            }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        clear()
    }

    fun finish() {
        scheduleId?.let {
            observation.storeData()
            observation.stopAndSetDone(it)
        }
        clear()
    }

    fun cancel() {
        scheduleId?.let {
            observation.stop(it)
        }
        clear()
    }

    fun clear() {
        _dataLoading.value = false
    }

    override fun close() {
        super.close()
        clear()
    }
}