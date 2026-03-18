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
package io.redlink.more.viewModels.login

import io.redlink.more.models.LoginModel
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.registration.RegistrationService
import io.redlink.more.viewModels.CoreViewModel

open class CoreLoginViewModel(private val registrationService: RegistrationService) :
    CoreViewModel() {

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

    override fun viewIdentifier(): String {
        return NavigationRoute.LOGIN.viewIdentifier
    }
}