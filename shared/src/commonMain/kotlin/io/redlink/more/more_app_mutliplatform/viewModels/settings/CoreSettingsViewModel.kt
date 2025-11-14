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
package io.redlink.more.more_app_mutliplatform.viewModels.settings

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.entities.StudyEntity
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine

class CoreSettingsViewModel(
    private val shared: Shared
) : CoreViewModel() {
    private val _dataDeleted = MutableStateFlow(false)

    @NativeCoroutines
    val dataDeleted: StateFlow<Boolean> = _dataDeleted

    private val _study = MutableStateFlow<StudyEntity?>(null)

    @NativeCoroutines
    val study: StateFlow<StudyEntity?> = _study
    private val _permissionModel = MutableStateFlow<PermissionModel?>(null)

    @NativeCoroutines
    val permissionModel: StateFlow<PermissionModel?> = _permissionModel

    init {
        launchScope {
            shared.repositories.study.study.combine(shared.repositories.observation.observations()) { study, observations ->
                Pair(study, observations)
            }.cancellable().collect {
                if (it.first?.active == true) {
                    _study.value = it.first
                    it.first?.let { study ->
                        _permissionModel.value =
                            PermissionModel.createFromSchema(study, it.second)
                    }
                }
            }
        }
    }

    fun exitStudy() {
        shared.exitStudy {
            _dataDeleted.value = true
        }
    }
}