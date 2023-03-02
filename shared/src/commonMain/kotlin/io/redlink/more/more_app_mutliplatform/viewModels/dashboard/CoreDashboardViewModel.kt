package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import kotlinx.coroutines.flow.Flow

class CoreDashboardViewModel {

    private val studyRepository: StudyRepository = StudyRepository()

    fun loadStudy(): Flow<List<StudySchema>> {
        return studyRepository.getStudy()
    }
}