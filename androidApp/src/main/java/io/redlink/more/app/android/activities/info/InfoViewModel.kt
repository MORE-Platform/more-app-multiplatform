package io.redlink.more.app.android.activities.info

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.activities.observations.questionnaire.QuestionnaireViewModel
import io.redlink.more.app.android.activities.setting.SettingsViewModel
import io.redlink.more.app.android.activities.studyDetails.StudyDetailsViewModel
import io.redlink.more.app.android.activities.tasks.TaskDetailsViewModel
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.app.android.services.bluetooth.AndroidBluetoothConnector
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.dashboard.CoreDashboardFilterViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion.SimpleQuestionCoreViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoViewModel: ViewModel() {
    private val studyCoreViewModel = CoreStudyDetailsViewModel()
    val studyTitle = mutableStateOf<String>("")

    // TODO: inforopository with infodata (not yet in bakcend) - exchange mock data to backend data after it exists
    val institute: String = "Ludwig Boltzmann Institute "
    val contactPerson: String = "Dr. Markus Mustermann"
    val contactEmail: String? = "markus.mustermann@bolzmann.at"
    val contactTel: String? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            studyCoreViewModel.studyModel.collect{
                withContext(Dispatchers.Main) {
                    studyTitle.value = it?.study?.studyTitle ?: ""
                }
            }
        }
    }
}