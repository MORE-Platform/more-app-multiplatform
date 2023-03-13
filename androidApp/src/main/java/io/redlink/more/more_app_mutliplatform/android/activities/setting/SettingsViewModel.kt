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
import io.redlink.more.more_app_mutliplatform.android.activities.setting.leave_study.LeaveStudyLvlOneView
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.*

class SettingsViewModel(): ViewModel() {
    private var coreSettingsViewModel: CoreSettingsViewModel? = null
    private val studySchema = mutableStateOf<StudySchema?>(null)
    private val studyRepository: StudyRepository = StudyRepository();
    var study: MutableState<StudySchema?> = mutableStateOf(StudySchema())
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val permissionModel =
        mutableStateOf(PermissionModel("Title", "Participation Info", emptyList()))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            studyRepository.getStudy().collect() {
                withContext(Dispatchers.Main) {
                    studySchema.value = it
                    it?.let { study ->
                        studySchema.value = study
                        permissionModel.value = PermissionModel.createFromSchema(it)
                    }
                }
            }
        }
    }

    fun createCoreViewModel(context: Context) {
        (context as? Activity)?.let {
            val storageRepository = SharedPreferencesRepository(it)
            coreSettingsViewModel = CoreSettingsViewModel(CredentialRepository(storageRepository), EndpointRepository(storageRepository))
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