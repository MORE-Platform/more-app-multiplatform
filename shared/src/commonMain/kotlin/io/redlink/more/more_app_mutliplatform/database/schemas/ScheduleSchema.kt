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
package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class ScheduleSchema : RealmObject {
    @PrimaryKey
    var scheduleId: ObjectId = ObjectId.invoke()
    var observationId: String = ""
    var observationType: String = ""
    var observationTitle: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var done: Boolean = false
    var hidden: Boolean = false
    var state: String = ScheduleState.DEACTIVATED.name

    fun getState() = ScheduleState.getState(state)

    fun updateState(specificState: ScheduleState? = null): ScheduleState {
        if (specificState != null) {
            state = specificState.name
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            (start?.epochSeconds?.times(1000))?.let { start ->
                (end?.epochSeconds?.times(1000))?.let { end ->
                    state = if (end <= now) {
                        if (getState().running()) {
                            ScheduleState.DONE.name
                        } else {
                            ScheduleState.ENDED.name
                        }
                    } else if (now < start) {
                        ScheduleState.DEACTIVATED.name
                    } else if (start <= now && !getState().running()) {
                        ScheduleState.ACTIVE.name
                    } else {
                        state
                    }
                }
            }
        }
        if (getState() == ScheduleState.DONE) {
            done = true
        }
        return getState()
    }

    fun equalsSchedule(other: ScheduleSchema): Boolean {
        return scheduleId == other.scheduleId
    }

    override fun toString(): String {
        return """ScheduleSchema: {"scheduleId": "$scheduleId", "observationId": "$observationId","observationType": "$observationType","observationTitle": "$observationTitle","start": "${start?.toString()}","end": "${end?.toString()}","done": $done,"hidden": $hidden,"state": "$state"}"""
    }


    companion object {
        fun toSchema(
            schedule: ObservationSchedule,
            observationId: String,
            observationType: String,
            observationTitle: String,
            hidden: Boolean
        ): ScheduleSchema? {
            return if (schedule.start != null && schedule.end != null) {
                val now = Clock.System.now().epochSeconds
                val scheduleState =
                    if (schedule.start.epochSeconds < now && schedule.end.epochSeconds > now) {
                        ScheduleState.ACTIVE
                    } else if (schedule.start.epochSeconds > now) {
                        ScheduleState.DEACTIVATED
                    } else {
                        ScheduleState.ENDED
                    }
                ScheduleSchema().apply {
                    this.observationId = observationId
                    this.observationType = observationType
                    this.observationTitle = observationTitle
                    start = schedule.start.toRealmInstant()
                    end = schedule.end.toRealmInstant()
                    this.hidden = hidden
                    state = scheduleState.name
                }
            } else null
        }
    }
}