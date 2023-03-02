package io.redlink.more.more_app_mutliplatform.viewModels.schedules

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.localDateTime
import io.redlink.more.more_app_mutliplatform.extensions.toDateMap
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class CoreScheduleViewModel {
    private val observationRepository = ObservationRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    val scheduleModelList: MutableStateFlow<Map<LocalDate, List<ScheduleModel>>> =
        MutableStateFlow(emptyMap())

    init {
        scope.launch {
            observationRepository.observationWithUndoneSchedules().collect { observationList ->
                scheduleModelList.value =
                    observationList.map { ScheduleModel.createModelsFrom(it) }.flatten().toDateMap()
            }
        }
    }

    fun onScheduleModelListChange(provideNewState: ((Map<LocalDate, List<ScheduleModel>>) -> Unit)): Closeable {
        return scheduleModelList.asClosure(provideNewState)
    }
}

