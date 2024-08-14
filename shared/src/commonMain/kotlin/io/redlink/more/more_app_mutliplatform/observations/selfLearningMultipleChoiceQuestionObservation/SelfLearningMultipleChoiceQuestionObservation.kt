package io.redlink.more.more_app_mutliplatform.observations.selfLearningMultipleChoiceQuestionObservation

import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SelfLearningMultipleChoiceQuestionObservationType

class SelfLearningMultipleChoiceQuestionObservation  : Observation(observationType = SelfLearningMultipleChoiceQuestionObservationType()){
    override fun start(): Boolean {
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        onCompletion()
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return false
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }
}