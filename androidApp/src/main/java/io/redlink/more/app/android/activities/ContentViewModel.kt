package io.redlink.more.app.android.activities

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.consent.ConsentViewModel
import io.redlink.more.app.android.activities.consent.ConsentViewModelListener
import io.redlink.more.app.android.activities.login.LoginViewModel
import io.redlink.more.app.android.activities.login.LoginViewModelListener
import io.redlink.more.app.android.activities.loginBLESetup.LoginBLESetupActivity
import io.redlink.more.app.android.activities.main.MainActivity
import io.redlink.more.app.android.extensions.showNewActivity
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ContentViewModel : ViewModel(), LoginViewModelListener, ConsentViewModelListener {
    private val registrationService: RegistrationService by lazy { RegistrationService(MoreApplication.shared!!) }

    val loginViewModel: LoginViewModel by lazy { LoginViewModel(registrationService, this) }
    val consentViewModel: ConsentViewModel by lazy { ConsentViewModel(registrationService, this) }

    val hasCredentials = mutableStateOf(MoreApplication.shared!!.credentialRepository.hasCredentials())
    val loginViewScreenNr = mutableStateOf(0)

    fun openMainActivity(context: Context) {
        (context as? Activity)?.let {
            MoreApplication.observationManager?.activateScheduleUpdate()
            MoreApplication.observationDataManager?.listenToDatapointCountChanges()
            val workManager = WorkManager.getInstance(context)
            val worker =
                PeriodicWorkRequestBuilder<ScheduleUpdateWorker>(15L, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                ScheduleUpdateWorker.WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                worker
            )
            showNewActivityAndClearStack(it, MainActivity::class.java)
        }
    }

    private fun showLoginView() {
        viewModelScope.launch(Dispatchers.Main) {
            loginViewScreenNr.value = 0
            registrationService.reset()
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