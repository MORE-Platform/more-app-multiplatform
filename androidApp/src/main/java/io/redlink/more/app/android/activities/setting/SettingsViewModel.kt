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
package io.redlink.more.app.android.activities.setting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.database.entities.StudyEntity
import io.redlink.more.models.PermissionModel
import io.redlink.more.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel : ViewModel() {
    val coreViewModel =
        CoreSettingsViewModel(
            MoreApplication.shared!!.repositories,
            MoreApplication.shared!!.sharedStorageRepository
        )
    val study = mutableStateOf<StudyEntity?>(null)
    val permissionModel = mutableStateOf<PermissionModel?>(null)

    init {
        coreViewModel.setExitStudyObserver(MoreApplication.shared)
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.study.collect {
                withContext(Dispatchers.Main) {
                    study.value = it
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.permissionModel.collect {
                withContext(Dispatchers.Main) {
                    permissionModel.value = it
                }
            }
        }
    }
}