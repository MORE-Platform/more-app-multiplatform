package io.redlink.more.more_app_mutliplatform.observations

interface DataRecorder {
    fun start(scheduleId: String)

    fun startMultiple(scheduleIds: Set<String>)
    fun pause(scheduleId: String)
    fun stop(scheduleId: String)
    fun stopAll()

    fun restartAll()
}