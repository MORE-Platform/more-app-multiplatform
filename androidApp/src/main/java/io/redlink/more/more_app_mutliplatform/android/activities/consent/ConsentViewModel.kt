package io.redlink.more.more_app_mutliplatform.android.activities.consent

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.extensions.getSecureID
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.services.extensions.toMD5
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.viewModels.permission.CorePermissionViewModel
import kotlinx.coroutines.*

private const val TAG = "ConsentViewModel"

interface ConsentViewModelListener {
    fun credentialsStored()
    fun decline()
}

class ConsentViewModel(
    registrationService: RegistrationService,
    private val consentViewModelListener: ConsentViewModelListener,
    context: Context
) : ViewModel() {
    private val coreModel = CorePermissionViewModel(registrationService)
    private var consentInfo: String? = null

    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val permissionModel = mutableStateOf(PermissionModel("Title", "Participation Info", emptyList()))
    val permissionsNotGranted = mutableStateOf(false)
    val permissions = mutableSetOf<String>()

    private val sharedPreferencesRepository = SharedPreferencesRepository(context)

    private val networkService = NetworkService(
        EndpointRepository(sharedPreferencesRepository),
        CredentialRepository(sharedPreferencesRepository)
    )
    private val observationFactory = AndroidObservationFactory(context)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreModel.permissionModel.collect {
                withContext(Dispatchers.Main) {
                    permissionModel.value = it
                    permissions.addAll(observationFactory.sensorPermissions())
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
    }

    fun getNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
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
