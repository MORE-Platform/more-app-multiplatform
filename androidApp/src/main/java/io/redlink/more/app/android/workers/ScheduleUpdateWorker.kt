package io.redlink.more.app.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.TimeUnit

class ScheduleUpdateWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    private val workManager = WorkManager.getInstance(applicationContext)
    override suspend fun doWork() = withContext(Dispatchers.IO) {
        val scheduleRepository = ScheduleRepository()
        scheduleRepository.updateTaskStates(MoreApplication.observationFactory ?: AndroidObservationFactory(applicationContext))
        scheduleRepository.nextSchedule().firstOrNull()?.let {
            val now = Instant.now().epochSecond
            if (now < it) {
                enqueueNewWorker(it - now)
            }
        }
        scheduleRepository.close()
        return@withContext Result.success()
    }

    private fun enqueueNewWorker(delay: Long) {
        val worker = OneTimeWorkRequestBuilder<ScheduleUpdateWorker>()
            .setInitialDelay(delay, TimeUnit.SECONDS)
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