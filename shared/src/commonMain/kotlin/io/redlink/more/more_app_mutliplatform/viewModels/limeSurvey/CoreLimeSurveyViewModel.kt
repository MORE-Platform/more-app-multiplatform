package io.redlink.more.more_app_mutliplatform.viewModels.limeSurvey

import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform

class CoreLimeSurveyViewModel(observationFactory: ObservationFactory): CoreViewModel() {
    private var observation: LimeSurveyObservation? = observationFactory.observation("lime-survey-observation") as? LimeSurveyObservation
    private var scheduleId: String? = null
    private var observationId: String? = null
    val limeSurveyLink: MutableStateFlow<String?> = MutableStateFlow(null)
    val dataLoading = MutableStateFlow(false)

    private val scheduleRepository = ScheduleRepository()
    private val observationRepository = ObservationRepository()

    fun setScheduleId(scheduleId: String) {
        if (scheduleId != this.scheduleId) {
            Napier.i { "Setting scheduleId: $scheduleId for LimeSurvey" }
            observation?.let { observation ->
                if (scheduleId.isNotEmpty() || scheduleId.isNotBlank()) {
                    this.scheduleId = scheduleId
                    dataLoading.set(true)
                    launchScope {
                        observation.limeURL.collect {
                            limeSurveyLink.set(it)
                        }
                    }
                    launchScope(Dispatchers.Main) {
                        scheduleRepository.scheduleWithId(scheduleId).cancellable().transform { scheduleSchema ->
                            Napier.i { "Loaded schedule schema!" }
                            emit(scheduleSchema?.let {
                                observationRepository.observationById(it.observationId).cancellable().firstOrNull()
                            })
                        }.cancellable().firstOrNull().let { observationSchema ->
                            log { "Loaded observation schema!" }
                            observationSchema?.let {
                                observationId = it.observationId
                                observation.observationConfig(it.configAsMap())
                                observation.start(it.observationId, scheduleId)
                            }
                            dataLoading.set(false)
                        }
                    }
                }

            }
        }
    }

    fun onLimeSurveyLinkChange(providedState: (String?) -> Unit) = limeSurveyLink.asClosure(providedState)

    fun onDataLoadingChange(providedState: (Boolean) -> Unit) = dataLoading.asClosure(providedState)

    override fun viewDidAppear() {

    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        clear()
    }

    fun finish() {
        scheduleId?.let {
            observation?.storeData()
            observation?.stopAndSetDone(it)
        }
        clear()
    }

    fun cancel() {
        scheduleId?.let {
            observation?.stop(it)
        }
        clear()
    }

    fun clear() {
        scheduleId = null
        observationId = null
        dataLoading.value = false
        limeSurveyLink.set(null)
    }

    override fun close() {
        super.close()
        clear()
    }
}