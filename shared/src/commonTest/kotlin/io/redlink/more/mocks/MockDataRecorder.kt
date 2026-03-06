package io.redlink.more.mocks

import io.redlink.more.observations.DataRecorder

class MockDataRecorder : DataRecorder {
    override fun start(scheduleId: String) {}
    override fun startMultiple(scheduleIds: Set<String>) {}
    override fun pause(scheduleId: String) {}
    override fun stop(scheduleId: String) {}
    override fun stopAll() {}
    override fun restartAll() {}
}