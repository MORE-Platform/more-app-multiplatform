package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable

class CoreDashboardViewModel: CoreViewModel() {
    private val studyRepository: StudyRepository = StudyRepository()
    val study: MutableStateFlow<StudySchema?> = MutableStateFlow(null)

    override fun viewDidAppear() {
        launchScope {
            studyRepository.getStudy().cancellable().collect {
                study.value = it
            }
        }
    }

    fun onLoadStudy(provideNewState: ((StudySchema?) -> Unit)) = study.asClosure(provideNewState)
}