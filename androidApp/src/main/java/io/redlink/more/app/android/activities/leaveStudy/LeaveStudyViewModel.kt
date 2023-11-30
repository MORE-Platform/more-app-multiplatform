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
package io.redlink.more.app.android.activities.leaveStudy

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaveStudyViewModel : ViewModel() {
    private var coreSettingsViewModel = CoreSettingsViewModel(MoreApplication.shared!!)
    val study = mutableStateOf<StudySchema?>(null)
    val permissionModel = mutableStateOf<PermissionModel?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            coreSettingsViewModel.study.collect {
                withContext(Dispatchers.Main) {
                    study.value = it
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            coreSettingsViewModel.permissionModel.collect {
                withContext(Dispatchers.Main) {
                    permissionModel.value = it
                }
            }
        }
    }

    fun viewDidAppear() {
        coreSettingsViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreSettingsViewModel.viewDidDisappear()
    }

    fun removeParticipation(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
        viewModelScope.launch {
            coreSettingsViewModel.dataDeleted.collect {
                if (it) {
                    (context as? Activity)?.let { activity ->
                        withContext(Dispatchers.Main) {
                            activity.finish()
                            showNewActivityAndClearStack(activity, ContentActivity::class.java)
                        }
                    }
                }
            }
        }
        coreSettingsViewModel.exitStudy()
    }
}