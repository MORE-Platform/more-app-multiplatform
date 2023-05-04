package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreDashboardViewModel: CoreViewModel() {
    private val studyRepository: StudyRepository = StudyRepository()
    val study: MutableStateFlow<StudySchema?> = MutableStateFlow(null)

    override fun viewDidAppear() {
        launchScope {
            studyRepository.getStudy().collect {
                study.value = it
            }
        }
    }

    fun onLoadStudy(provideNewState: ((StudySchema?) -> Unit)): Closeable {
        return study.asClosure(provideNewState)
    }
}