package io.redlink.more.more_app_mutliplatform.viewModels.studydetails

import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CoreStudyDetailsViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val studyRepository: StudyRepository = StudyRepository()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    val studyModel = MutableStateFlow<StudyDetailsModel?>(null)
    init {
        scope.launch {
            studyRepository.getStudy()
                .combine(scheduleRepository.allSchedulesWithStatus(true)) { study, doneTasks ->
                    Pair(study, doneTasks.size)
                }.combine(scheduleRepository.count()) { firstPair, taskCount ->
                    Triple(
                        firstPair.first,
                        firstPair.second,
                        taskCount
                    )
                }.combine(observationRepository.observations()) { triple, observations ->
                    Pair(
                        triple,
                        observations
                    )
                }.collect {
                    it.first.first?.let {studySchema ->
                        studyModel.value = StudyDetailsModel.createModelFrom(studySchema, it.second, it.first.third,
                            it.first.second.toLong()
                        )
                    }
                }
        }
    }

    fun onLoadStudyDetails(provideNewState: ((StudyDetailsModel?) -> Unit)): Closeable {
        return studyModel.asClosure(provideNewState)
    }
}