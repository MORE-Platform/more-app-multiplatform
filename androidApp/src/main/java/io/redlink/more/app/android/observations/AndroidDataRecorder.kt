package io.redlink.more.app.android.observations

import android.content.Context
import io.redlink.more.app.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder

class AndroidDataRecorder(private val context: Context): DataRecorder {
    override fun start(scheduleId: String) {
        ObservationRecordingService.start(context, scheduleId)
    }

    override fun pause(scheduleId: String) {
        ObservationRecordingService.pause(context, scheduleId)
    }

    override fun stop(scheduleId: String) {
        ObservationRecordingService.stop(context, scheduleId)
    }

    override fun stopAll() {
        ObservationRecordingService.stopAll(context)
    }
}