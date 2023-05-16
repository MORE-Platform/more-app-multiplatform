package io.redlink.more.more_app_mutliplatform.models

enum class ScheduleState {
    DEACTIVATED,
    ACTIVE,
    RUNNING,
    PAUSED,
    DONE,
    ENDED
    ;

    fun active() = this == ACTIVE || this == RUNNING || this == PAUSED

    fun running() = this == RUNNING || this == PAUSED

    fun completed() = this == ENDED || this == DONE

    companion object {
        fun getState(name: String) = ScheduleState.values().firstOrNull { it.name == name } ?: DEACTIVATED
    }
}