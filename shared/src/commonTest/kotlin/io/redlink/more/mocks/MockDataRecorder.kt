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
package io.redlink.more.mocks

import io.redlink.more.observations.DataRecorder

open class MockDataRecorder : DataRecorder {
    var startCalled = false
    var startMultipleCalled = false
    var pauseCalled = false
    var stopCalled = false

    override fun start(scheduleId: String) {
        startCalled = true
    }

    override fun startMultiple(scheduleIds: Set<String>) {
        startMultipleCalled = true
    }

    override fun pause(scheduleId: String) {
        pauseCalled = true
    }

    override fun stop(scheduleId: String) {
        stopCalled = true
    }

    override fun stopAll() {}
    override fun restartAll() {}
}