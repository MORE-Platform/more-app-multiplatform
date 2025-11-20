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
package io.redlink.umm.participant.viewModels.login

import io.redlink.umm.participant.models.LoginModel
import io.redlink.umm.participant.registration.RegistrationService
import io.redlink.umm.participant.viewModels.CoreViewModel

class CoreLoginViewModel(private val registrationService: RegistrationService) : CoreViewModel() {

    fun sendRegistrationToken(
        loginModel: LoginModel
    ) {
        if (loginModel.valid()) {
            registrationService.sendRegistrationToken(
                loginModel
            )
        }
    }

    fun clearError() {
        registrationService.clearError()
    }
}