package io.redlink.more.more_app_mutliplatform.viewModels.login

import io.ktor.utils.io.core.*
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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