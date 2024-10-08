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
package io.redlink.more.more_app_mutliplatform.viewModels.permission

import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Observation
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class CorePermissionViewModel(
    private val registrationService: RegistrationService,
    private val studyConsentTitle: String
) : CoreViewModel() {
    val permissionModel: MutableStateFlow<PermissionModel> = MutableStateFlow(
        PermissionModel(
            "Title",
            "info",
            "consent info",
            consentInfo = emptyList()
        )
    )
    val loadingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val observations: MutableStateFlow<List<Observation>> = MutableStateFlow(emptyList())

    fun buildConsentModel() {
        registrationService.study?.let {
            permissionModel.value = PermissionModel.create(it, studyConsentTitle)
            observations.value = it.observations
        }
    }

    fun acceptConsent(
        consentInfoMd5: String,
        uniqueDeviceId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (NetworkServiceError?) -> Unit
    ) {
        loadingFlow.value = true
        registrationService.acceptConsent(consentInfoMd5, uniqueDeviceId, onSuccess, onError) {
            loadingFlow.value = false
        }
    }

    fun declineConsent() {
        registrationService.declineConsent()
    }

    fun onConsentModelChange(provideNewState: ((PermissionModel) -> Unit)) =
        permissionModel.asClosure(provideNewState)

    fun onLoadingChange(provideNewState: ((Boolean) -> Unit)) =
        loadingFlow.asClosure(provideNewState)

    override fun viewDidAppear() {

    }
}