package io.redlink.more.app.android.observations

import io.github.aakira.napier.Napier
import io.redlink.more.app.android.services.ObservationRecordingService
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder

class AndroidDataRecorder : DataRecorder {
    override fun start(scheduleId: String) {
        Napier.i { "Starting scheduleId: $scheduleId" }
        ObservationRecordingService.start(setOf(scheduleId))
    }

    override fun startMultiple(scheduleIds: Set<String>) {
        Napier.i { "Starting schedule Ids: $scheduleIds" }
        ObservationRecordingService.start(scheduleIds)
    }

    override fun pause(scheduleId: String) {
        Napier.i { "Pause schedule Id: $scheduleId" }
        ObservationRecordingService.pause(scheduleId)
    }

    override fun stop(scheduleId: String) {
        Napier.i { "Stopping schedule Id: $scheduleId" }
        ObservationRecordingService.stop(scheduleId)
    }

    override fun stopAll() {
        Napier.i { "Stopping all Schedules!" }
        ObservationRecordingService.stopAll()
    }

    override fun restartAll() {
        Napier.i { "Restarting all schedules!" }
        ObservationRecordingService.restartAll()
    }
}