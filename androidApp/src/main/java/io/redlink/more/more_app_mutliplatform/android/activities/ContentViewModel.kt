package io.redlink.more.more_app_mutliplatform.android.activities

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.more_app_mutliplatform.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModelListener
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.login.LoginViewModelListener
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardView
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentViewModel : ViewModel(), LoginViewModelListener, ConsentViewModelListener {
    private val sharedPreferencesRepository: SharedStorageRepository = SharedPreferencesRepository(context = MoreApplication.appContext!!)
    private val registrationService: RegistrationService = RegistrationService(sharedPreferencesRepository)
    private val credentialRepository: CredentialRepository = CredentialRepository(sharedPreferencesRepository)

    val loginViewModel: LoginViewModel = LoginViewModel(registrationService, this)
    val consentViewModel: ConsentViewModel = ConsentViewModel(registrationService, this)

    val hasCredentials = mutableStateOf(credentialRepository.hasCredentials())
    val loginViewScreenNr = mutableStateOf(0)

    private fun showLoginView() {
        viewModelScope.launch(Dispatchers.Main) {
            loginViewScreenNr.value = 0
            registrationService.reset()
        }
    }


    fun openDashboard(context: Context) {
        (context as? Activity)?.let {
            showNewActivityAndClearStack(it, DashboardView::class.java)
        }
    }

    private fun showConsentView() {
        viewModelScope.launch(Dispatchers.Main) {
            loginViewScreenNr.value = 1
        }
    }

    override fun tokenIsValid(study: Study) {
        this.consentViewModel.setConsentInfo(study.consentInfo)
        this.consentViewModel.buildConsentModel()
        showConsentView()
    }

    override fun credentialsStored() {
        viewModelScope.launch(Dispatchers.Main) {
            hasCredentials.value = true
        }
    }

    override fun decline() {
        showLoginView()
    }
}