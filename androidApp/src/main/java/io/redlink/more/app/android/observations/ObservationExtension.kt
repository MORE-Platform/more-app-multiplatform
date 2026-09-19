/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.app.android.observations

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.ObservationType

fun Observation.showPermissionAlertDialog(missingPermissions: List<String> = emptyList()) {
    var message = stringResource(R.string.required_permission_not_granted_message)
    if (missingPermissions.isNotEmpty()) {
        message += "\n\n" + stringResource(R.string.missing_permissions_label) + ": " + missingPermissions.joinToString(", ")
    }
    AlertController.openAlertDialog(
        AlertDialogModel(
            title = StringDesc.Raw(stringResource(R.string.required_permissions_not_granted_title)),
            message = StringDesc.Raw(message),
            confirmLabel = StringDesc.Raw(stringResource(R.string.proceed_to_settings_button)),
            cancelLabel = StringDesc.Raw(stringResource(R.string.proceed_without_granting_button)),
            onConfirm = {
                MoreApplication.openSettings.value = true
            }
        ))
}

fun Observation.pauseObservation(observationType: ObservationType) {
    MoreApplication.shared!!.observationManager.pauseObservationType(
        observationType.observationType
    )
}