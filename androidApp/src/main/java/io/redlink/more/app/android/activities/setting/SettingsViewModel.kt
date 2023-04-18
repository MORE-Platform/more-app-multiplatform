package io.redlink.more.more_app_mutliplatform.android.activities.setting

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.activities.ContentActivity
import io.redlink.more.more_app_mutliplatform.android.activities.setting.leave_study.LeaveStudyLevelOneActivity
import io.redlink.more.more_app_mutliplatform.android.activities.setting.leave_study.LeaveStudyLevelTwoActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(context: Context): ViewModel() {
    private val storageRepository = SharedPreferencesRepository(context)
    private var coreSettingsViewModel = CoreSettingsViewModel(CredentialRepository(storageRepository), EndpointRepository(storageRepository))
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

    fun removeParticipation(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            coreSettingsViewModel.dataDeleted.collect {
                if (it) {
                    (context as? Activity)?.let { activity ->
                        activity.finish()
                        showNewActivityAndClearStack(activity, ContentActivity::class.java)
                    }
                }
            }
        }
        coreSettingsViewModel.exitStudy()
    }

    fun openLeaveStudyLvlOne(context: Context) {
        (context as? Activity)?.let {
            showNewActivity(it, LeaveStudyLevelOneActivity::class.java)
        }
    }
    fun openLeaveStudyLvlTwo(context: Context) {
        (context as? Activity)?.let {
            showNewActivity(it, LeaveStudyLevelTwoActivity::class.java)
        }
    }
}