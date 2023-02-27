package io.redlink.more.more_app_mutliplatform.android.activities.setting

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.activities.ContentActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.viewModels.settings.CoreSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SettingsViewModel(): ViewModel() {
    private var coreSettingsViewModel: CoreSettingsViewModel? = null

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
}