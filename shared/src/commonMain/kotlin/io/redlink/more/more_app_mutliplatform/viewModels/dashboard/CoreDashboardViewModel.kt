package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CoreDashboardViewModel {

    private val studyRepository: StudyRepository = StudyRepository()
    val study: MutableStateFlow<StudySchema?> = MutableStateFlow(null)
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    init {
        scope.launch {
            studyRepository.getStudy().collect {
                study.value = it
            }
        }
    }

    fun onLoadStudy(provideNewState: ((StudySchema?) -> Unit)): Closeable {
        return study.asClosure(provideNewState)
    }
}