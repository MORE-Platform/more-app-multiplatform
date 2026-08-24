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
package io.redlink.more.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.redlink.more.extensions.toStudyState
import io.redlink.more.models.StudyState
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.util.createUUID
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@Entity(tableName = "studies")
data class StudyEntity(
    @PrimaryKey
    val studyId: String = createUUID(),
    val studyTitle: String = "",
    val participantId: Int? = null,
    val participantAlias: String? = "",
    val participantInfo: String = "",
    val consentInfo: String = "",
    val start: Long? = null,
    val end: Long? = null,
    val contactInstitute: String? = null,
    val contactPerson: String? = null,
    val contactEmail: String? = null,
    val contactPhoneNumber: String? = null,
    val version: Long = 0,
    val active: Boolean = false,
    val state: String = (if (active) StudyState.ACTIVE else StudyState.PAUSED).descr,
    val finishText: String? = null
) {
    @Ignore
    fun getState() = StudyState.getState(state)

    @Ignore
    fun startInstant() = start?.let { Instant.fromEpochSeconds(it) }

    @Ignore
    fun endInstant() = end?.let { Instant.fromEpochSeconds(it) }

    companion object {
        fun fromStudy(study: Study): StudyEntity {
            val active = study.active ?: false
            return StudyEntity(
                studyTitle = study.studyTitle,
                consentInfo = study.consentInfo,
                participantInfo = study.participantInfo,
                participantId = study.participant?.id,
                participantAlias = study.participant?.alias,
                start = study.start.atStartOfDayIn(TimeZone.currentSystemDefault())
                    .epochSeconds,
                end = study.end.atStartOfDayIn(TimeZone.currentSystemDefault())
                    .epochSeconds,
                contactInstitute = study.contact?.institute,
                contactPerson = study.contact?.person,
                contactEmail = study.contact?.email,
                contactPhoneNumber = study.contact?.phoneNumber,
                version = study.version,
                active = active,
                state = (study.studyState?.toStudyState()
                    ?: if (active) StudyState.ACTIVE else StudyState.PAUSED).descr,
                finishText = study.finishText
            )
        }
    }
}
