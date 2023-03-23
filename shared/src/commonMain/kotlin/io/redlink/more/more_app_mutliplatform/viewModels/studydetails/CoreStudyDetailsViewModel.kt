package io.redlink.more.more_app_mutliplatform.viewModels.studydetails

import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.StudyDetailsModel
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreStudyDetailsViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val studyRepository: StudyRepository = StudyRepository()
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()

    val studyTitle: MutableStateFlow<String> = MutableStateFlow("Study Title")
    val participantInfo: MutableStateFlow<String> = MutableStateFlow("Participant Info")
    val start: MutableStateFlow<Long> = MutableStateFlow(0)
    val end: MutableStateFlow<Long> = MutableStateFlow(0)
    val observations: MutableStateFlow<RealmList<ObservationSchema>> = MutableStateFlow(realmListOf())

    val finishedTasks: MutableStateFlow<Long> = MutableStateFlow(0)
    val totalTasks: MutableStateFlow<Long> = MutableStateFlow(0)

    val studyDetailsModel: MutableStateFlow<StudyDetailsModel?> = MutableStateFlow(null)

    fun loadStudy(){
        scope.launch {
            studyRepository.getStudy().collect{ study ->
                study?.let {
                    studyDetailsModel.value = StudyDetailsModel.createModelFrom(it)
                }
            }
        }

//        scope.launch {
//            studyRepository.getStudy().collect { study ->
//                study?.let {
//                    studyTitle.emit(it.studyTitle)
//                    participantInfo.emit(it.participantInfo)
//                    start.emit(it.start?.epochSeconds ?: 0)
//                    end.emit(it.end?.epochSeconds ?: 0)
//                    observations.emit(it.observations)
//                }
//            }
//        }
        scope.launch {
            scheduleRepository.count().collect{
                totalTasks.value = it
            }
        }
        scope.launch {
            scheduleRepository.allSchedulesWithStatus(true).collect{
                finishedTasks.value = it.size.toLong()
            }
        }
    }
    fun onLoadStudyDetails(provideNewState: ((StudyDetailsModel?) -> Unit)): Closeable {
        return studyDetailsModel.asClosure(provideNewState)
    }

    fun onStudyTitle(provideNewState: ((String?) -> Unit)): Closeable {
        return studyTitle.asClosure(provideNewState)
    }
    fun onParticipantInfo(provideNewState: ((String?) -> Unit)): Closeable {
        return participantInfo.asClosure(provideNewState)
    }
    fun onObservations(provideNewState: ((RealmList<ObservationSchema>?) -> Unit)): Closeable {
        return observations.asClosure(provideNewState)
    }
    fun onStart(provideNewState: ((Long?) -> Unit)): Closeable {
        return start.asClosure(provideNewState)
    }
    fun onEnd(provideNewState: ((Long?) -> Unit)): Closeable {
        return end.asClosure(provideNewState)
    }
    fun onFinishedTasks(provideNewState: ((Long?) -> Unit)): Closeable {
        return finishedTasks.asClosure(provideNewState)
    }
    fun onTotalTasks(provideNewState: ((Long?) -> Unit)): Closeable {
        return totalTasks.asClosure(provideNewState)
    }
}