package io.redlink.more.app.android.observations

import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType

fun Observation.showPermissionAlertDialog() {
    AlertController.openAlertDialog(
        AlertDialogModel(
            title = stringResource(R.string.required_permissions_not_granted_title),
            message = stringResource(R.string.required_permission_not_granted_message),
            confirmLabel = stringResource(R.string.proceed_to_settings_button),
            cancelLabel = stringResource(R.string.proceed_without_granting_button),
            onConfirm = {
                MoreApplication.openSettings.value = true
            }
        ))
}

fun Observation.Companion.pauseObservation(observationType: ObservationType) {
    MoreApplication.shared!!.observationManager.pauseObservationType(
        observationType.observationType
    )
}