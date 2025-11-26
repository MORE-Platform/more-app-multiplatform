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
package io.redlink.umm.blendedcare.app.android.activities.consent

import android.content.Context
import androidx.lifecycle.ViewModel
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.extensions.getSecureID
import io.redlink.umm.blendedcare.app.android.extensions.stringResource
import io.redlink.umm.participant.AlertController
import io.redlink.umm.participant.models.AlertDialogModel
import io.redlink.umm.participant.registration.RegistrationService
import io.redlink.umm.participant.viewModels.permission.CoreConsentViewModel

class ConsentViewModel(
    val registrationService: RegistrationService
) : ViewModel() {
    val coreModel =
        CoreConsentViewModel(registrationService, stringResource(R.string.consent_information))

    fun acceptConsent(context: Context) {
        getSecureID(context)?.let { uniqueDeviceId ->
            registrationService.acceptConsent(uniqueDeviceId)
        }
    }

    fun openPermissionDeniedAlertDialog(context: Context) {
        AlertController.openAlertDialog(
            AlertDialogModel(
                title = stringResource(R.string.required_permissions_not_granted_title),
                message = stringResource(R.string.required_permission_not_granted_message),
                confirmLabel = stringResource(R.string.proceed_to_settings_button),
                cancelLabel = stringResource(R.string.proceed_without_granting_button),
                onConfirm = {
                    BlendedCareApplication.openSettings.value = true
                },
                onDecline = {
                    acceptConsent(context)
                }
            ))
    }

    fun openNotificationPermissionDeniedAlertDialog(context: Context) {
        AlertController.openAlertDialog(
            AlertDialogModel(
                title = stringResource(R.string.notification_permission_not_granted_title),
                message = stringResource(R.string.notification_permission_not_granted_message),
                confirmLabel = stringResource(R.string.proceed_to_settings_button),
                cancelLabel = stringResource(R.string.proceed_without_granting_button),
                onConfirm = {
                    BlendedCareApplication.openSettings.value = true
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
