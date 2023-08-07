package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema

enum class StudyState(val descr: String) {
    NONE("none"),
    ACTIVE("active"),
    PAUSED("paused"),
    CLOSED("closed");

    companion object {
        fun getState(name: String) = StudyState.values().firstOrNull { it.descr == name } ?: NONE
    }
}