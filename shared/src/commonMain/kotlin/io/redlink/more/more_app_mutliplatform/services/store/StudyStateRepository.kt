package io.redlink.more.more_app_mutliplatform.services.store

import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.StudyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudyStateRepository(private val sharedStorageRepository: SharedStorageRepository) {
    private var currentStudyState: MutableStateFlow<StudyState> = MutableStateFlow(StudyState.NONE)

    init {
        currentStudyState.set(loadState())
    }

    fun storeState(studyState: StudyState) {
        sharedStorageRepository.store(STUDY_STATE_KEY, studyState.descr)
        currentStudyState.set(studyState)
    }

    private fun loadState(): StudyState {
        return StudyState.getState(sharedStorageRepository.load(STUDY_STATE_KEY, "none"))
    }

    fun currentState() = currentStudyState.asStateFlow()

    companion object {
        private val STUDY_STATE_KEY = "studyStateKey"
    }
}