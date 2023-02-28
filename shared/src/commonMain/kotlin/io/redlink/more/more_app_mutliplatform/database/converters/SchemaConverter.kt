package io.redlink.more.more_app_mutliplatform.database.converters

import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class SchemaConverter {

    companion object SchemaConverter {
        fun toSchema(study: Study): StudySchema {
            return StudySchema().apply {
                studyTitle = study.studyTitle
                consentInfo = study.consentInfo
                participantInfo = study.participantInfo
                observations = study.observations.map { toSchema(it) }.toRealmList()
                start = RealmInstant.from(
                    study.start.atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds(), 0
                )
                end = RealmInstant.from(
                    study.end.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                    0
                )
                version = study.version
            }
        }

        private fun toSchema(observation: Observation): ObservationSchema {
            return ObservationSchema().apply {
                observationId = observation.observationId
                observationTitle = observation.observationTitle
                observationType = observation.observationType
                participantInfo = observation.participantInfo
                configuration = observation.configuration.toString()
                schedule = observation.schedule.map { toSchema(it, observation.observationId) }
                    .toRealmList()
            }
        }

        private fun toSchema(schedule: ObservationSchedule, id: String): ScheduleSchema {
            return ScheduleSchema().apply {
                observationId = id
                start = schedule.start?.let {
                    RealmInstant.from(it.toEpochMilliseconds(), 0)
                }
                end = schedule.end?.let {
                    RealmInstant.from(it.toEpochMilliseconds(), 0)
                }
            }
        }
    }
}