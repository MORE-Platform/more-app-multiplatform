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
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.mongodb.kbson.ObjectId

class StudySchema : RealmObject {
    @PrimaryKey
    var studyId: ObjectId = ObjectId.invoke()
    var studyTitle: String = ""
    var participantId: Int? = null
    var participantAlias: String? = ""
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
    var state: String = (if(active) StudyState.ACTIVE else StudyState.PAUSED).descr
    var finishText: String? = null

    fun getState() = StudyState.getState(state)

    companion object {
        fun toSchema(study: Study): StudySchema {
            return StudySchema().apply {
                studyTitle = study.studyTitle
                consentInfo = study.consentInfo
                participantInfo = study.participantInfo
                participantId = study.participant?.id
                participantAlias = study.participant?.alias
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
                state = (study.studyState?.let { StudyState.getState(it) } ?: if (active) StudyState.ACTIVE else StudyState.PAUSED).descr
                finishText = study.finishText
            }
        }
    }
}