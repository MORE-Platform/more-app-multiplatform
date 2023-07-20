package io.redlink.more.more_app_mutliplatform.observations.extensions.pushButtonObservation

import io.redlink.more.more_app_mutliplatform.observations.Observation

class PushButtonObservation : Observation(observationType = PushButtonObservationType()){
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