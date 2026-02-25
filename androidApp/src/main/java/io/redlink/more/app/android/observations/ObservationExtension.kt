package io.redlink.more.app.android.observations

import io.redlink.more.AlertController
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.models.AlertDialogModel
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.ObservationType
import io.redlink.umm.blendedcare.app.android.R

fun Observation.showPermissionAlertDialog() {
    AlertController.openAlertDialog(
        AlertDialogModel(
            title = stringResource(R.string.required_permissions_not_granted_title),
            message = stringResource(R.string.required_permission_not_granted_message),
            confirmLabel = stringResource(R.string.proceed_to_settings_button),
            cancelLabel = stringResource(R.string.proceed_without_granting_button),
            onConfirm = {
                MoreApplication.Companion.openSettings.value = true
            }
        ))
}

fun Observation.Companion.pauseObservation(observationType: ObservationType) {
    MoreApplication.Companion.shared!!.observationManager.pauseObservationType(
        observationType.observationType
    )
}