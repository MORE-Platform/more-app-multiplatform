package io.redlink.more.more_app_mutliplatform.database.converters

import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.Study
import io.redlink.more.more_app_mutliplatform.database.schemas.Observation
import io.redlink.more.more_app_mutliplatform.database.schemas.Schedule
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationSchedule
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class SchemaConverter {

    companion object SchemaParser {
        fun toSchema(study: io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study): Study {
            return Study().apply {
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

        private fun toSchema(observation: io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation): Observation {
            return Observation().apply {
                observationId = observation.observationId
                observationTitle = observation.observationTitle
                observationType = observation.observationType
                participantInfo = observation.participantInfo
                configuration = observation.configuration.toString()
                schedule = observation.schedule.map { toSchema(it, observation.observationId) }
                    .toRealmList()
            }
        }

        private fun toSchema(schedule: ObservationSchedule, id: String): Schedule {
            return Schedule().apply {
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