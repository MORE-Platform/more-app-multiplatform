package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation


class ObservationSchema : RealmObject {
    @PrimaryKey
    var observationId: String = ""
    var observationType: String = ""
    var observationTitle: String = ""
    var participantInfo: String = ""
    var configuration: String? = null
    var schedule: RealmList<ScheduleSchema> = realmListOf()
    var hidden: Boolean? = null
    var version: Long = 0
    var required: Boolean = false

    companion object {
        fun toSchema(observation: Observation): ObservationSchema {
            return ObservationSchema().apply {
                observationId = observation.observationId
                observationTitle = observation.observationTitle
                observationType = observation.observationType
                participantInfo = observation.participantInfo
                configuration = observation.configuration.toString()
                hidden = observation.hidden
                required = observation.required
                version = observation.version
                schedule = observation.schedule.map {
                    ScheduleSchema.toSchema(
                        it,
                        observation.observationId
                    )
                }
                    .toRealmList()
            }
        }
    }
}