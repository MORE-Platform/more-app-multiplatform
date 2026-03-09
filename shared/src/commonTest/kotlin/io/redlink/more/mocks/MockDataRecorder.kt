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