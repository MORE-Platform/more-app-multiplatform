package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study


data class PermissionModel(
    val studyTitle: String,
    val studyParticipantInfo: String,
    val consentInfo: List<PermissionConsentModel>
) {
    companion object {
        fun create(study: Study): PermissionModel {
            val observationConsent = mutableListOf<PermissionConsentModel>()
            observationConsent.add(PermissionConsentModel(study.studyTitle, study.consentInfo))
            observationConsent.addAll( study.observations.map { PermissionConsentModel(it.observationTitle, it.participantInfo) })
            return PermissionModel(study.studyTitle, study.participantInfo, observationConsent)
        }
    }
}