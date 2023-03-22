package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

class StudyDetailsViewModel(private val coreViewModel: CoreStudyDetailsViewModel) {
    val studyTitle: MutableState<String> = mutableStateOf("")
    val participantInfo: MutableState<String> = mutableStateOf("")
    val start: MutableState<Date> = mutableStateOf(Date())
    val end: MutableState<Date> = mutableStateOf(Date())
    val observations: MutableState<RealmList<ObservationSchema>> = mutableStateOf(realmListOf())
    val finishedTasks: MutableState<Long> = mutableStateOf(0)
    val totalTasks: MutableState<Long> = mutableStateOf(0)

    init {
        coreViewModel.loadStudy()
        studyTitle.value = coreViewModel.studyTitle.value
        participantInfo.value = coreViewModel.participantInfo.value
        start.value = coreViewModel.start.value.toDate()
        end.value = coreViewModel.end.value.toDate()
        observations.value = coreViewModel.observations.value
        finishedTasks.value = coreViewModel.finishedTasks.value
        totalTasks.value = coreViewModel.totalTasks.value
    }
}