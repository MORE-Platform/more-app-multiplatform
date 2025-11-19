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
package io.redlink.umm.participant.models

import io.redlink.umm.participant.database.entities.ObservationEntity
import io.redlink.umm.participant.database.entities.StudyEntity
import io.redlink.umm.participant.services.network.openapi.model.Study

data class PermissionModel(
    val studyTitle: String,
    val studyParticipantInfo: String,
    val studyConsentInfo: String,
    val consentInfo: List<PermissionConsentModel>
) {
    companion object {
        fun create(study: Study, studyConsentTitle: String): PermissionModel {
            val observationConsent = mutableListOf<PermissionConsentModel>()
            observationConsent.add(PermissionConsentModel(studyConsentTitle, study.consentInfo))
            observationConsent.addAll(
                study.observations.sortedBy { it.observationTitle }
                    .map { PermissionConsentModel(it.observationTitle, it.participantInfo) }
            )
            return PermissionModel(
                study.studyTitle,
                study.participantInfo,
                study.consentInfo,
                observationConsent
            )
        }

        fun createFromSchema(
            studySchema: StudyEntity,
            observations: List<ObservationEntity>
        ): PermissionModel {
            val observationConsent = mutableListOf<PermissionConsentModel>()
            observationConsent.add(
                PermissionConsentModel(
                    studySchema.studyTitle,
                    studySchema.consentInfo
                )
            )
            observationConsent.addAll(observations.map {
                PermissionConsentModel(
                    it.observationTitle,
                    it.participantInfo
                )
            })
            return PermissionModel(
                studySchema.studyTitle,
                studySchema.participantInfo,
                studySchema.consentInfo,
                observationConsent
            )
        }
    }
}