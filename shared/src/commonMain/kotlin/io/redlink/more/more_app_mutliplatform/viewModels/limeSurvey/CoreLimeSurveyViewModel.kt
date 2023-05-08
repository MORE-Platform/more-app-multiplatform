package io.redlink.more.more_app_mutliplatform.viewModels.limeSurvey

import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

class CoreLimeSurveyViewModel(private val observationManager: ObservationManager): CoreViewModel() {
    private var observation: LimeSurveyObservation? = null
    private var scheduleId: String? = null
    val limeSurveyLink = MutableStateFlow<String?>(null)

    fun setScheduleId(scheduleId: String) {
        if (scheduleId.isNotEmpty() || scheduleId.isNotBlank()) {
            this.scheduleId = scheduleId
            launchScope(Dispatchers.Main) {
                if (observationManager.start(scheduleId)) {
                    observation =
                        observationManager.getObservationForScheduleId(scheduleId) as? LimeSurveyObservation
                    observation?.let { observation ->
                        if (observation.observerAccessible()) {
                            launchScope(Dispatchers.Main) {
                                observation.limeURL.collect {
                                    limeSurveyLink.emit(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun viewDidAppear() {

    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        clear()
    }

    fun addData() {
        observation?.storeData("")
    }

    fun finish() {
        observation?.stopAndSetDone()
    }

    fun clear() {
//        scheduleId?.let {
//            observationManager.stop(it)
//        }
        scheduleId = null
        observation = null
        launchScope {
            limeSurveyLink.emit(null)
        }
    }

    override fun close() {
        super.close()
        clear()
    }
}