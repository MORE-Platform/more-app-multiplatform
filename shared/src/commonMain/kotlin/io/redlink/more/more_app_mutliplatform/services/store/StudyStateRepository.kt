/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.services.store

import io.redlink.more.more_app_mutliplatform.models.StudyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock

class StudyStateRepository(private val sharedStorageRepository: SharedStorageRepository) {
    private var _currentStudyState: MutableStateFlow<StudyState> = MutableStateFlow(StudyState.NONE)
    val currentStudyState: StateFlow<StudyState> = _currentStudyState

    private var _lastUpdateTime: MutableStateFlow<Long?> = MutableStateFlow(null)
    val lastUpdateTime: StateFlow<Long?> = _lastUpdateTime

    init {
        _currentStudyState.value = loadState()
        _lastUpdateTime.value = loadUpdateTime()
    }

    fun storeState(studyState: StudyState) {
        sharedStorageRepository.store(STUDY_STATE_KEY, studyState.descr)
        val currentTime = Clock.System.now().epochSeconds
        sharedStorageRepository.store(LAST_UPDATE_TIME_KEY, currentTime.toString())
        _currentStudyState.value = studyState
        _lastUpdateTime.value = currentTime
    }

    fun studyWasUpdatedBefore(epochSeconds: Long): Boolean =
        (lastUpdateTime.value ?: 0) <= epochSeconds

    private fun loadState(): StudyState {
        return StudyState.getState(sharedStorageRepository.load(STUDY_STATE_KEY, "none"))
    }

    private fun loadUpdateTime(): Long? {
        val storedValue = sharedStorageRepository.load(LAST_UPDATE_TIME_KEY, "")
        return if (storedValue.isNotEmpty()) {
            storedValue.toLongOrNull()
        } else {
            null
        }
    }

    companion object {
        private const val STUDY_STATE_KEY = "studyStateKey"
        private const val LAST_UPDATE_TIME_KEY = "lastUpdateTimeKey"
    }
}