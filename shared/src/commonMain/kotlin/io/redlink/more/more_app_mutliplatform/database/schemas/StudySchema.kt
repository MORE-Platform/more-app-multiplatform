package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.datetime.*

class StudySchema : RealmObject {
    var studyTitle: String = ""
    var participantInfo: String = ""
    var consentInfo: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var observations: RealmList<ObservationSchema> = realmListOf()
    var version: Long = 0
    var active: Boolean? = null

    companion object {
        fun toSchema(study: Study): StudySchema {
            return StudySchema().apply {
                studyTitle = study.studyTitle
                consentInfo = study.consentInfo
                participantInfo = study.participantInfo
                observations =
                    study.observations.map { ObservationSchema.toSchema(it) }.toRealmList()
                start = Instant.fromEpochMilliseconds(
                    study.start.atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                ).toRealmInstant()
                end = Instant.fromEpochMilliseconds(
                    study.end.atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                ).toRealmInstant()
                version = study.version
            }
        }
    }
}