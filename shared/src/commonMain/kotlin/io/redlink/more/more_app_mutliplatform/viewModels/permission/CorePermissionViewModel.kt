package io.redlink.more.more_app_mutliplatform.viewModels.permission

import io.ktor.utils.io.core.*
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CorePermissionViewModel(
    private val registrationService: RegistrationService
) {
    val permissionModel: MutableStateFlow<PermissionModel> = MutableStateFlow(PermissionModel("Title", "info", consentInfo = emptyList()))
    val loadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun buildConsentModel() {
        registrationService.study?.let {
            permissionModel.value = PermissionModel.create(it)
        }
    }

    fun acceptConsent(consentInfoMd5: String, uniqueDeviceId: String, onSuccess: (Boolean) -> Unit, onError: (NetworkServiceError?) -> Unit) {
        loadingFlow.value = true
        registrationService.acceptConsent(consentInfoMd5, uniqueDeviceId, onSuccess, onError) {
            loadingFlow.value = false
        }
    }

    fun onConsentModelChange(provideNewState: ((PermissionModel) -> Unit)): Closeable {
        val job = Job()
        permissionModel.onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object : Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }

    fun onLoadingChange(provideNewState: ((Boolean) -> Unit)): Closeable {
        val job = Job()
        loadingFlow.onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object : Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}