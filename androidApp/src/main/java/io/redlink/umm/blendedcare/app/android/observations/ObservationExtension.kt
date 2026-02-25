package io.redlink.umm.blendedcare.app.android.observations

import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.extensions.stringResource
import io.redlink.umm.participant.AlertController
import io.redlink.umm.participant.models.AlertDialogModel
import io.redlink.umm.participant.observations.Observation
import io.redlink.umm.participant.observations.observationTypes.ObservationType

fun Observation.showPermissionAlertDialog() {
    AlertController.openAlertDialog(
        AlertDialogModel(
            title = stringResource(R.string.required_permissions_not_granted_title),
            message = stringResource(R.string.required_permission_not_granted_message),
            confirmLabel = stringResource(R.string.proceed_to_settings_button),
            cancelLabel = stringResource(R.string.proceed_without_granting_button),
            onConfirm = {
                BlendedCareApplication.openSettings.value = true
            }
        ))
}

fun Observation.Companion.pauseObservation(observationType: ObservationType) {
    BlendedCareApplication.shared!!.observationManager.pauseObservationType(
        observationType.observationType
    )
}