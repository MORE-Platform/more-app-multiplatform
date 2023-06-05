package io.redlink.more.app.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.AndroidObservationDataManager
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.TimeUnit

class ScheduleUpdateWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    override suspend fun doWork() = withContext(Dispatchers.IO) {
        Napier.d { "Running $WORKER_TAG! Updating Schedule..." }
        val observationDataManager = MoreApplication.shared?.observationDataManager ?: AndroidObservationDataManager(applicationContext)
        val observationFactory = MoreApplication.shared?.observationFactory ?: AndroidObservationFactory(applicationContext, observationDataManager)
        val dataRecorder = MoreApplication.shared?.dataRecorder ?: AndroidDataRecorder()
        ScheduleRepository().updateTaskStatesSync(observationFactory, dataRecorder)
        return@withContext Result.success()
    }

    companion object {
        const val WORKER_TAG = "ScheduleUpdateWorker"
    }
}