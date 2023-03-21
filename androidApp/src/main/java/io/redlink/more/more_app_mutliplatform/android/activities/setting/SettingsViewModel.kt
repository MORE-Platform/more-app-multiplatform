package io.redlink.more.more_app_mutliplatform.android.activities.setting

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.MutableState
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

class SettingsViewModel(): ViewModel() {
    private var coreSettingsViewModel: CoreSettingsViewModel? = null
    private val studySchema = mutableStateOf<StudySchema?>(null)
    var study: MutableState<StudySchema?> = mutableStateOf(StudySchema())

    val permissionModel =
        mutableStateOf(PermissionModel("Title", "Participation Info", "Study Consent Info", emptyList()))

    fun createCoreViewModel(context: Context) {
        (context as? Activity)?.let {
            val storageRepository = SharedPreferencesRepository(it)
            coreSettingsViewModel = CoreSettingsViewModel(CredentialRepository(storageRepository), EndpointRepository(storageRepository))
            viewModelScope.launch(Dispatchers.IO) {
                coreSettingsViewModel?.let {
                    it.loadStudy().collect { study ->
                        withContext(Dispatchers.Main) {
                            study?.let {
                                studySchema.value = study
                                permissionModel.value = PermissionModel.createFromSchema(study)
                            }
                        }
                    }
                }
            }
        }
    }

    fun removeParticipation(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            coreSettingsViewModel?.dataDeleted?.collect {
                if (it) {
                    (context as? Activity)?.let { activity ->
                        activity.finish()
                        showNewActivityAndClearStack(activity, ContentActivity::class.java)
                    }
                }
            }
        }
        coreSettingsViewModel?.exitStudy()
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