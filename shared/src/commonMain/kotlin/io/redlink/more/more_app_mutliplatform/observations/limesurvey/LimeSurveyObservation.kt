package io.redlink.more.more_app_mutliplatform.observations.limesurvey

import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.LimeSurveyType

class LimeSurveyObservation: Observation(observationType = LimeSurveyType()) {
    var limeSurveyId: String? = null
        private set
    var token: String? = null
        private set
    var limeSurveyLink: String? = null
        private set

    override fun start(): Boolean {
        return limeSurveyId != null && token != null && limeSurveyLink != null
    }

    override fun stop(onCompletion: () -> Unit) {
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        return true
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        limeSurveyId = settings[LIMESURVEY_ID] as? String
        token = settings[LIMESURVEY_TOKEN] as? String
        limeSurveyLink = settings[LIMESURVEY_URL] as? String
    }

    override fun needsToRestartAfterAppClosure(): Boolean {
        return true
    }

    companion object {
        const val LIMESURVEY_ID = "limeSurveyId"
        const val LIMESURVEY_TOKEN = "token"
        const val LIMESURVEY_URL = "limeUrl"
    }
}