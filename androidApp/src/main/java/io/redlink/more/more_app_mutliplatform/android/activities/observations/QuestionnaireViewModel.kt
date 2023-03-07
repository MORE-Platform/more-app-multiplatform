package io.redlink.more.more_app_mutliplatform.android.activities.observations

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivity
import io.redlink.more.more_app_mutliplatform.android.extensions.showNewActivityAndClearStack
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class QuestionnaireViewModel: ViewModel() {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    val observationTitle = mutableStateOf("")
    val observationParticipantInfo = mutableStateOf("")

    val answers = mutableStateListOf<String>()
    val answerSet = mutableStateOf(false)

    val question = mutableStateOf("Question missing!")

    private fun finished(setObservationToDone: Boolean = true) {
        //observerManagement.stop(this, setObservationToDone)
    }

    fun returnToMain(context: Context) {
        showNewActivityAndClearStack(context, DashboardActivity::class.java)
    }

    fun goToNextActivity(context: Context) {
        finished()
        showNewActivity(context, QuestionnaireResponseActivity::class.java)
        (context as? Activity)?.finish()
    }

    fun closeActivity(context: Context, setObservationToDone: Boolean) {
        finished(setObservationToDone)
        (context as? Activity)?.finish()
    }
}