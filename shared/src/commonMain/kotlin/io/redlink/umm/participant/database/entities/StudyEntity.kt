package io.redlink.umm.participant.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.redlink.umm.blendedcare.services.network.openapi.model.Study
import io.redlink.umm.participant.extensions.toStudyState
import io.redlink.umm.participant.models.StudyState
import io.redlink.umm.participant.util.createUUID
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
