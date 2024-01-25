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
package io.redlink.more.app.android.activities.consent

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getSecureID
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.services.extensions.toMD5
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.viewModels.permission.CorePermissionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ConsentViewModelListener {
    fun credentialsStored()
    fun decline()
}

class ConsentViewModel(
    registrationService: RegistrationService,
    private val consentViewModelListener: ConsentViewModelListener
) : ViewModel() {
    private val coreModel = CorePermissionViewModel(registrationService, stringResource(R.string.consent_information))
    private var consentInfo: String? = null

    val permissionModel =
        mutableStateOf(PermissionModel("Title", "Participation Info", "Study Consent Info", emptyList()))
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val permissionsNotGranted = mutableStateOf(false)
    val permissions = mutableSetOf<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreModel.permissionModel.collect {
                withContext(Dispatchers.Main) {
                    permissionModel.value = it
                    permissions.addAll(MoreApplication.shared!!.observationFactory.studySensorPermissions())
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
                        MoreApplication.shared!!.newLogin()
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
