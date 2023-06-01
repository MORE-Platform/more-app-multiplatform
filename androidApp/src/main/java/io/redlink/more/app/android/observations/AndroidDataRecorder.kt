package io.redlink.more.app.android.observations

import io.redlink.more.app.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder

class AndroidDataRecorder : DataRecorder {
    override fun start(scheduleId: String) {
        ObservationRecordingService.start(scheduleId)
    }

    override fun pause(scheduleId: String) {
        ObservationRecordingService.pause(scheduleId)
    }

    override fun stop(scheduleId: String) {
        ObservationRecordingService.stop(scheduleId)
    }

    override fun stopAll() {
        ObservationRecordingService.stopAll()
    }

    override fun restartAll() {
        ObservationRecordingService.restartAll()
    }
}