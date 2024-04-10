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
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.viewModels.login.CoreLoginViewModel
import kotlinx.coroutines.*

interface LoginViewModelListener {
    fun tokenIsValid(study: Study)
}

class LoginViewModel(registrationService: RegistrationService, private val loginViewModelListener: LoginViewModelListener) : ViewModel() {
    private val coreLoginViewModel = CoreLoginViewModel(registrationService)

    private val tokenValid = mutableStateOf(false)
    val participantKey = mutableStateOf("")
    val loadingState = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val dataEndpoint = mutableStateOf("");
    val defaultEndpoint = mutableStateOf(registrationService.getEndpointRepository().endpoint())
    val endpointError = mutableStateOf<String?>(null)

    fun participationKeyNotBlank(): Boolean = this.participantKey.value.isNotBlank()
    fun isTokenError(): Boolean = !this.error.value.isNullOrBlank()
    fun isEndpointError(): Boolean = !this.endpointError.value.isNullOrBlank()
    fun tokenIsValid() = this.tokenValid.value

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreLoginViewModel.loadingFlow.collect {
                withContext(Dispatchers.Main) {
                    loadingState.value = it
                }
            }
        }
    }

    fun viewDidAppear() {
        coreLoginViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreLoginViewModel.viewDidDisappear()
    }

    fun currentEndpoint() = dataEndpoint.value.ifEmpty { defaultEndpoint.value }

    fun validateKey() {
        coreLoginViewModel.sendRegistrationToken(participantKey.value, dataEndpoint.value.ifEmpty { null },
            onSuccess = {
                        loginViewModelListener.tokenIsValid(it)
            }, onError = {
                error.value = it?.message
            })
    }

}