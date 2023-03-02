package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CoreDashboardViewModel {

    private val studyRepository: StudyRepository = StudyRepository()

    fun loadStudy(): MutableStateFlow<StudySchema?> {
        val study: MutableStateFlow<StudySchema?> = MutableStateFlow(StudySchema())
        CoroutineScope(Dispatchers.Default + Job()).launch {
            studyRepository.getStudy().collect {
                study.value = it
            }
        }
        return study
    }

    fun onLoadStudy(provideNewState: ((StudySchema?) -> Unit)): Closeable {
        val job = Job()
        loadStudy().onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object: Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}