package io.redlink.more.more_app_mutliplatform.android.activities.consent

import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.getSecureID
import io.redlink.more.more_app_mutliplatform.managers.ObservationsManager
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.observations.ObservationType
import io.redlink.more.more_app_mutliplatform.services.extensions.toMD5
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation
import io.redlink.more.more_app_mutliplatform.viewModels.permission.CorePermissionViewModel
import kotlinx.coroutines.*

private const val TAG = "ConsentViewModel"

interface ConsentViewModelListener {
    fun credentialsStored()
    fun decline()
}

class ConsentViewModel(
    registrationService: RegistrationService,
    private val consentViewModelListener: ConsentViewModelListener
) : ViewModel() {
    private val coreModel = CorePermissionViewModel(registrationService)
    private var consentInfo: String? = null

    val permissionModel =
        mutableStateOf(PermissionModel("Title", "Participation Info", emptyList()))
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val permissionsNotGranted = mutableStateOf(false)
    val permissions = mutableSetOf<String>()
    val observations = mutableStateOf<List<Observation>?>(null)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreModel.permissionModel.collect {
                withContext(Dispatchers.Main) {
                    permissionModel.value = it
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            coreModel.loadingFlow.collect {
                withContext(Dispatchers.Main) {
                    loading.value = it
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            coreModel.observations.collect {
                withContext(Dispatchers.Main) {
                    observations.value = it
                }
            }
        }
    }

    fun getNeededPermissions(obs: List<Observation>) {
        obs.let { observations ->
            permissions += ObservationsManager
                .getAllPermissionsForFactories(observations.map { it.observationType })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ObservationType.NOTIFICATION.sensorPermission?.let { permissions.add(it) }
            }
        }
    }

    fun setConsentInfo(info: String) {
        this.consentInfo = info
    }

    fun acceptConsent(context: Context) {
        consentInfo?.let { info ->
            getSecureID(context)?.let { uniqueDeviceId ->
                coreModel.acceptConsent(info.toMD5(), uniqueDeviceId,
                    onSuccess = {
                        consentViewModelListener.credentialsStored()
                    }, onError = {
                        error.value = it?.message
                    })
            }
        }
    }

    fun decline() {
        consentViewModelListener.decline()
    }

    fun buildConsentModel() {
        coreModel.buildConsentModel()
    }
}
