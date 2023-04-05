package io.redlink.more.more_app_mutliplatform.models

enum class ScheduleState {
    DEACTIVATED,
    ACTIVE,
    RUNNING,
    PAUSED,
    DONE,
    ENDED
    ;

    companion object {
        fun getState(name: String) = ScheduleState.values().firstOrNull { it.name == name } ?: DEACTIVATED
    }
}