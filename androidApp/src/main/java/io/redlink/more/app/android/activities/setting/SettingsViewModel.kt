package io.redlink.more.app.android.activities.setting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel : ViewModel() {
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
}