package io.redlink.more.app.android.workers

import android.content.Context
import androidx.work.*
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.TimeUnit

class ScheduleUpdateWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    private val scheduleRepository = ScheduleRepository()
    private val observationFactory = AndroidObservationFactory(applicationContext)
    private val workManager = WorkManager.getInstance(applicationContext)
    override suspend fun doWork() = withContext(Dispatchers.IO) {
        scheduleRepository.updateTaskStates(observationFactory)
        scheduleRepository.nextSchedule().firstOrNull()?.let {
            val now = Instant.now().epochSecond
            if (now < it) {
                enqueueNewWorker(it - now)
            }
        }
        return@withContext Result.success()
    }

    private fun enqueueNewWorker(delay: Long) {
        val worker = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            WORKER_TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            worker)
    }

    companion object {
        const val WORKER_TAG = "ScheduleUpdateWorker"
    }
}