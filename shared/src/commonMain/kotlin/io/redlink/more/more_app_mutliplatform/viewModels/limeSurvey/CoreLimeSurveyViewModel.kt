package io.redlink.more.more_app_mutliplatform.viewModels.limeSurvey

import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

class CoreLimeSurveyViewModel(private val observationManager: ObservationManager): CoreViewModel() {
    private var observation: LimeSurveyObservation? = null
    private var scheduleId: String? = null

    val link = MutableStateFlow<String?>(null)
    val surveyId = MutableStateFlow<String?>(null)
    val token = MutableStateFlow<String?>(null)

    fun setScheduleId(scheduleId: String) {
        this.scheduleId = scheduleId
        launchScope {
            if (observationManager.start(scheduleId)) {
                observation = observationManager.getObservationForScheduleId(scheduleId) as? LimeSurveyObservation
            }
        }
    }

    override fun viewDidAppear() {
        observation?.let { observation ->
            if (observation.observerAccessible()) {
                launchScope(Dispatchers.Main) {
                    link.emit(observation.limeSurveyLink)
                    surveyId.emit(observation.limeSurveyId)
                    token.emit(observation.token)
                }
            }
        }
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()
        clear()
    }

    fun addData() {
        observation?.storeData("")
    }

    fun clear() {
        observation?.stopAndSetDone()
        scheduleId?.let {
            observationManager.stop(it)
        }
        scheduleId = null
        observation = null
        launchScope {
            token.emit(null)
            link.emit(null)
            surveyId.emit(null)
        }
    }

    override fun close() {
        super.close()
        clear()
    }
}