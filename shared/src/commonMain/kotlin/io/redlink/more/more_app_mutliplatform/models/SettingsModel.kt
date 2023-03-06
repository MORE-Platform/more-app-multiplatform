package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study


data class SettingsModel (
    val studyTitle: String,
    val observationConsent: List<PermissionConsentModel>,
    val settingsTitle: String,
    val settingsDescription: String,
)
