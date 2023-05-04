package io.redlink.more.more_app_mutliplatform.observations

interface DataRecorder {
    fun start(scheduleId: String)
    fun pause(scheduleId: String)
    fun stop(scheduleId: String)
    fun stopAll()

    fun restartAll()
    fun updateTaskStates()
}