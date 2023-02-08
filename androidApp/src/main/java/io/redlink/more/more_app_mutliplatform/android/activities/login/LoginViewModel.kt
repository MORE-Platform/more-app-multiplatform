package io.redlink.more.more_app_mutliplatform.android.activities.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val tokenValid = mutableStateOf(false)
    val participantKey = mutableStateOf("")
    val loadingState = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    val dataEndpoint = mutableStateOf("https://data.platform-test.more.redlink.io/api/v1")
    val endpointItemOpen = mutableStateOf(false)
    val endpointError = mutableStateOf<String?>(null)

    fun participationKeyNotBlank(): Boolean = this.participantKey.value.isNotBlank()
    fun isTokenError(): Boolean = !this.error.value.isNullOrBlank()
    fun isEndpointError(): Boolean = !this.endpointError.value.isNullOrBlank()
    fun tokenIsValid() = this.tokenValid.value

    fun toggleEndpointTextField() {
        endpointItemOpen.value = !endpointItemOpen.value
    }

//    fun validateKey() {
//        if (participationKeyNotBlank()) {
//            if (dataEndpoint.value.isNotEmpty() || dataEndpoint.value != sharedPreferencesRepository.getDataEndpoint()) {
//                if (!sharedPreferencesRepository.setDataEndpoint(dataEndpoint.value)) {
//                    endpointError.value = "Server URL invalid!"
//                    loadingState.value = false
//                    return
//                }
//            }
//            else if (dataEndpoint.value.isEmpty()) {
//                sharedPreferencesRepository.setDataEndpoint(DATA_BASE_PATH_ENDPOINT)
//            }
//            scope.launch {
//                workManager.cancelAllWork()
//                dataPointRepository.removeAllDataPoints()
//            }
//            scope.launch {
//                val success =
//                    registrationService.sendRegistrationToken(participantKey.value.uppercase())
//                if (!success) {
//                    error.value = registrationService.error?.message
//                } else {
//                    tokenValid.value = true
//                }
//                loadingState.value = false
//            }
//        }
//    }

}