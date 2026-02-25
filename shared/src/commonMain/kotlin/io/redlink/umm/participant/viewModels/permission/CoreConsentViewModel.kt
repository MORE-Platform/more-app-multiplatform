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
package io.redlink.umm.participant.viewModels.permission

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.umm.participant.extensions.mapState
import io.redlink.umm.participant.models.PermissionModel
import io.redlink.umm.participant.registration.RegistrationService
import io.redlink.umm.participant.viewModels.CoreViewModel
import kotlinx.coroutines.flow.StateFlow

class CoreConsentViewModel(
    registrationService: RegistrationService,
    private val studyConsentTitle: String
) : CoreViewModel() {
    @NativeCoroutines
    val permissions: StateFlow<PermissionModel?> =
        registrationService.study.mapState(viewModelScope, null) { study ->
            study?.let { PermissionModel.create(it, studyConsentTitle) }
        }
}