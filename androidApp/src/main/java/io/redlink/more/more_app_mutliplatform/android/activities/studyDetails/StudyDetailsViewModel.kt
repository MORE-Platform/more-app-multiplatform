package io.redlink.more.more_app_mutliplatform.android.activities.studyDetails

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.viewModels.studydetails.CoreStudyDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date

class StudyDetailsViewModel(private val coreViewModel: CoreStudyDetailsViewModel) {
    val start: MutableState<Date> = mutableStateOf(Date())
    val end: MutableState<Date> = mutableStateOf(Date())
    val studyDetailsModel: MutableState<StudyDetailsModel> = mutableStateOf(
        StudyDetailsModel("", "", "", 0, 0, emptyList(), 0, false, 0, 0)
    )

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        scope.launch {
            coreViewModel.loadStudy()
            coreViewModel.studyDetailsModel.collect{ details ->
                details?.let {
                    studyDetailsModel.value = it
                }
            }
        }
        start.value = (coreViewModel.studyDetailsModel.value?.start?.let { Instant.ofEpochMilli(it).epochSecond }
            ?.times(1000))?.toDate() ?: Date(0)
        end.value = (coreViewModel.studyDetailsModel.value?.end?.let { Instant.ofEpochMilli(it).epochSecond }
            ?.times(1000))?.toDate() ?: Date(0)
    }
}