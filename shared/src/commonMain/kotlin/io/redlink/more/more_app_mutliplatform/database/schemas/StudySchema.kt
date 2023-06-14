package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyContact
import kotlinx.datetime.*
import org.mongodb.kbson.ObjectId

class StudySchema : RealmObject {
    @PrimaryKey
    var studyId: ObjectId = ObjectId.invoke()
    var studyTitle: String = ""
    var participantInfo: String = ""
    var consentInfo: String = ""
    var start: RealmInstant? = null
    var end: RealmInstant? = null
    var contactInstitute: String? = null
    var contactPerson: String? = null
    var contactEmail: String? = null
    var contactPhoneNumber: String? = null
    var version: Long = 0
    var active: Boolean = false

    companion object {
        fun toSchema(study: Study): StudySchema {
            return StudySchema().apply {
                studyTitle = study.studyTitle
                consentInfo = study.consentInfo
                participantInfo = study.participantInfo
                start = Instant.fromEpochMilliseconds(
                    study.start.atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                ).toRealmInstant()
                end = Instant.fromEpochMilliseconds(
                    study.end.atStartOfDayIn(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                ).toRealmInstant()
                contactInstitute = study.contact?.institute
                contactPerson = study.contact?.person
                contactEmail = study.contact?.email
                contactPhoneNumber = study.contact?.phoneNumber
                version = study.version
                active = study.active ?: false
            }
        }
    }
}