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
package io.redlink.more.more_app_mutliplatform.models

enum class ScheduleState {
    DEACTIVATED,
    ACTIVE,
    RUNNING,
    PAUSED,
    DONE,
    ENDED
    ;

    fun deactivated() = this == DEACTIVATED

    fun active() = this == ACTIVE || this == RUNNING || this == PAUSED

    fun running() = this == RUNNING || this == PAUSED

    fun completed() = this == ENDED || this == DONE

    companion object {
        fun getState(name: String) = entries.firstOrNull { it.name == name } ?: DEACTIVATED
    }
}