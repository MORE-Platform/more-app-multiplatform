package io.redlink.more.more_app_mutliplatform.viewModels.login

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_multiplatform.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CoreLoginViewModel {
    private val networkService = NetworkService()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val studyFlow: MutableStateFlow<Study?> = MutableStateFlow(null)

    fun sendRegistrationToken(token: String) {
        if (token.isNotEmpty()) {
            val upperCaseToken = token.uppercase()
            scope.launch {
                val (study, networkError) = networkService.validateRegistrationToken(upperCaseToken)
                studyFlow.value = study

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

}