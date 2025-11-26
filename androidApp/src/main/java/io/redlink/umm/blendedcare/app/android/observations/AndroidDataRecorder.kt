/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.umm.blendedcare.app.android.observations

import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.services.ObservationRecordingService
import io.redlink.umm.participant.observations.DataRecorder

class AndroidDataRecorder : DataRecorder {
    override fun start(scheduleId: String) {
        Napier.i { "Starting scheduleId: $scheduleId" }
        ObservationRecordingService.Companion.start(setOf(scheduleId))
    }

    override fun startMultiple(scheduleIds: Set<String>) {
        Napier.i { "Starting schedule Ids: $scheduleIds" }
        ObservationRecordingService.Companion.start(scheduleIds)
    }

    override fun pause(scheduleId: String) {
        Napier.i { "Pause schedule Id: $scheduleId" }
        ObservationRecordingService.Companion.pause(scheduleId)
    }

    override fun stop(scheduleId: String) {
        Napier.i { "Stopping schedule Id: $scheduleId" }
        ObservationRecordingService.Companion.stop(scheduleId)
    }

    override fun stopAll() {
        Napier.i { "Stopping all Schedules!" }
        ObservationRecordingService.Companion.stopAll()
    }

    override fun restartAll() {
        Napier.i { "Restarting all schedules!" }
        ObservationRecordingService.Companion.restartAll()
    }
}