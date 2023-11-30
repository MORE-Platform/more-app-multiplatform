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
package io.redlink.more.more_app_mutliplatform.viewModels.login

import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class CoreLoginViewModel(private val registrationService: RegistrationService): CoreViewModel() {

    val loadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun sendRegistrationToken(token: String, endpoint: String? = null, onSuccess: (Study) -> Unit, onError: (NetworkServiceError?) -> Unit) {
        if (token.isNotEmpty()) {
            loadingFlow.value = true
            registrationService.sendRegistrationToken(token.uppercase(), endpoint, onSuccess, onError) {
                loadingFlow.value = false
            }
        }
    }

    fun onLoadingChange(provideNewState: ((Boolean) -> Unit)) = loadingFlow.asClosure(provideNewState)

    override fun viewDidAppear() {

    }
}