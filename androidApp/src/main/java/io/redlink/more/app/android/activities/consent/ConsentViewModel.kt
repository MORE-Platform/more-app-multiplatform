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
package io.redlink.more.app.android.activities.consent

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getSecureID
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.registration.RegistrationService
import io.redlink.more.viewModels.permission.CoreConsentViewModel

class ConsentViewModel(
    val registrationService: RegistrationService,
    private val onConsentAccepted: (() -> Unit)? = null
) : ViewModel() {
    val coreModel =
        CoreConsentViewModel(registrationService, stringResource(R.string.consent_information))

    fun acceptConsent(context: Context) {
        if (onConsentAccepted != null) {
            // The caller wants to interject a step before registration completes (the Polar
            // profile form); it is responsible for calling acceptConsent itself afterwards.
            onConsentAccepted.invoke()
        } else {
            getSecureID(context)?.let { uniqueDeviceId ->
                registrationService.acceptConsent(uniqueDeviceId)
            }
        }
    }

    fun openPermissionDeniedAlertDialog(context: Context, missingPermissions: List<String> = emptyList()) {
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
                },
                onDecline = {
                    acceptConsent(context)
                }
            ))
    }

    fun openNotificationPermissionDeniedAlertDialog(context: Context) {
        AlertController.openAlertDialog(
            AlertDialogModel(
                title = StringDesc.Raw(stringResource(R.string.notification_permission_not_granted_title)),
                message = StringDesc.Raw(stringResource(R.string.notification_permission_not_granted_message)),
                confirmLabel = StringDesc.Raw(stringResource(R.string.proceed_to_settings_button)),
                cancelLabel = StringDesc.Raw(stringResource(R.string.proceed_without_granting_button)),
                onConfirm = {
                    MoreApplication.openSettings.value = true
                },
                onDecline = {
                    acceptConsent(context)
                }
            ))
    }

    fun decline() {
        registrationService.declineConsent()
    }
}
