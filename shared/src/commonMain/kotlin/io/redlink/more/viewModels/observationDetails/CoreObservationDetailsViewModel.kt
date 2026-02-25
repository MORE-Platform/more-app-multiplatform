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
package io.redlink.more.viewModels.observationDetails

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.asClosure
import io.redlink.more.models.ObservationDetailsModel
import io.redlink.more.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine

class CoreObservationDetailsViewModel(
    private val repository: MainRepository,
    private val observationId: String
) : CoreViewModel() {
    val observationDetailsModel = MutableStateFlow<ObservationDetailsModel?>(null)

    override fun viewDidAppear() {
        launchScope {
            repository.observation.observationById(observationId)
                .combine(repository.schedule.getFirstAndLastDate(observationId)) { observation, pair ->
                    Triple(
                        observation,
                        pair.first,
                        pair.second
                    )
                }.cancellable().collect { triple ->
                    triple.first?.let { observation ->
                        observationDetailsModel.value =
                            ObservationDetailsModel.Companion.createModelFrom(
                                observation,
                                triple.second,
                                triple.third
                            )
                    }
                }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        launchScope {
            observationDetailsModel.emit(null)
        }
    }

    fun onLoadObservationDetails(provideNewState: ((ObservationDetailsModel?) -> Unit)): Closeable {
        return observationDetailsModel.asClosure(provideNewState)
    }
}