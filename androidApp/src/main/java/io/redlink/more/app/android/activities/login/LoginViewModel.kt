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
package io.redlink.more.app.android.activities.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.io.more.app.android.R
import io.redlink.more.AlertController
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.models.AlertDialogModel
import io.redlink.more.models.LoginModel
import io.redlink.more.registration.RegistrationService
import io.redlink.more.util.validateAndNormalizeUrl

class LoginViewModel(
    val registrationService: RegistrationService
) : ViewModel() {
    val participantKey = mutableStateOf("")
    val isLoading = registrationService.isLoading
    val error = registrationService.error

    val dataEndpoint = mutableStateOf("")
    val defaultEndpoint = mutableStateOf(registrationService.getEndpointRepository().endpoint())
    val endpointError = mutableStateOf<String?>(null)

    fun participationKeyNotBlank(): Boolean = this.participantKey.value.isNotBlank()
    fun isEndpointError(): Boolean =
        !this.endpointError.value.validateAndNormalizeUrl().isNullOrBlank()

    fun currentEndpoint() = dataEndpoint.value.ifEmpty { defaultEndpoint.value }

    fun validateKey() {
        if (registrationService.connected.value) {
            val loginModel = LoginModel(participantKey.value, currentEndpoint())
            if (loginModel.valid()) {
                registrationService.sendRegistrationToken(loginModel)
            } else {
                AlertController.openAlertDialog(
                    AlertDialogModel(
                        stringResource(R.string.more_token_error),
                        stringResource(R.string.more_404),
                        confirmLabel = "Ok"
                    )
                )
            }
        } else {
            val dialogModel = AlertDialogModel(
                title = stringResource(R.string.no_internet_connection_title),
                message = stringResource(R.string.no_internet_connection_body),
                confirmLabel = "Ok"
            )
            AlertController.openAlertDialog(dialogModel)
        }
    }

    fun extractValuesFromQRCode(qrCodeUrl: String) {
        dataEndpoint.value =
            qrCodeUrl.substringBefore("signup?").validateAndNormalizeUrl() ?: currentEndpoint()
        participantKey.value =
            qrCodeUrl.substringAfter("token=", "").takeIf { it.isNotEmpty() } ?: ""
    }
}