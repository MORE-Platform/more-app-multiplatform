package io.redlink.more.more_app_mutliplatform.viewModels.tasks

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.DataPointCountRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.CoreScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CoreTaskDetailsViewModel(private val coreScheduleViewModel: CoreScheduleViewModel) {

    private val dataPointCountRepository: DataPointCountRepository = DataPointCountRepository()

    fun loadDataCount(scheduleId: String): Flow<DataPointCountSchema?> {
        return dataPointCountRepository.get(scheduleId)
    }

    fun onLoadDataCount(scheduleId: String, provideNewState: ((DataPointCountSchema?) -> Unit)): Closeable {
        val job = Job()
        loadDataCount(scheduleId).onEach {
            provideNewState(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))
        return object: Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }

    fun startObservation(scheduleId: String, observationId: String, type: String) {
        coreScheduleViewModel.start(scheduleId, observationId, type)
    }

    fun pause(scheduleId: String) {
        coreScheduleViewModel.pause(scheduleId)
    }

    fun stop(scheduleId: String) {
        coreScheduleViewModel.stop(scheduleId)
    }
}