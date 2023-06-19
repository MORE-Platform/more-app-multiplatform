package io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation

import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.SimpleQuestionType

class SimpleQuestionObservation : Observation(observationType = SimpleQuestionType()){
    override fun start(): Boolean {
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        return true
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return false
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }
}