package io.redlink.more.more_app_mutliplatform.viewModels.login

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.redlink.more.more_app_multiplatform.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TAG = "CoreLoginViewModel"
class CoreLoginViewModel(endpointRepository: EndpointRepository, credentialRepository: CredentialRepository) {
    private val networkService = NetworkService(endpointRepository, credentialRepository)
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    val studyFlow: MutableStateFlow<Study?> = MutableStateFlow(null)
    val loadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun sendRegistrationToken(token: String, endpoint: String? = null) {
        if (token.isNotEmpty()) {
            val upperCaseToken = token.uppercase()
            loadingFlow.value = true
            scope.launch {
                val (study, networkError) = networkService.validateRegistrationToken(upperCaseToken)
                study?.let {
                    studyFlow.value = study
                    Napier.d("Study received", tag = TAG)
                }
                networkError?.let {
                    Napier.e("Network Error", tag = TAG)
                }
            }
        }
    }

    fun onStudyChange(provideNewState: ((Study?) -> Unit)): Closeable {
        val job = Job()
        studyFlow.onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope( Dispatchers.Main + job))
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
        return object: Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }

}