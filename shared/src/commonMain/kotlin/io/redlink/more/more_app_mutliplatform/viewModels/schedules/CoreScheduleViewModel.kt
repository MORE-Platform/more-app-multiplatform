package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.extensions.*
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

class CoreScheduleViewModel {
    private val observationRepository = ObservationRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    val scheduleModelList: MutableStateFlow<Map<Long, List<ScheduleModel>>> =
        MutableStateFlow(emptyMap())

    init {
        scope.launch {
            observationRepository.observationWithUndoneSchedules().collect { observationList ->
                scheduleModelList.value = createMap(observationList)
            }
        }
    }

    private fun createMap(observationList: List<ObservationSchema>): Map<Long, List<ScheduleModel>> {
        return observationList
            .asSequence()
            .map { ScheduleModel.createModelsFrom(it) }
            .flatten()
            .filter { it.end > Clock.System.now().toEpochMilliseconds() }
            .sortedBy { it.start }
            .groupBy { it.start.toLocalDate().time() }
    }

    fun onScheduleModelListChange(provideNewState: ((Map<Long, List<ScheduleModel>>) -> Unit)): Closeable {
        return scheduleModelList.asClosure(provideNewState)
    }
}

