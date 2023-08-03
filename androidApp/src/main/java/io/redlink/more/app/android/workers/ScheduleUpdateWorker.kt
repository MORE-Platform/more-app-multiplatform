package io.redlink.more.app.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleUpdateWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val shared: Shared

    init {
        if (MoreApplication.shared == null) {
            MoreApplication.initShared(applicationContext)
        }
        shared = MoreApplication.shared!!
    }

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        Napier.i { "Running $WORKER_TAG! Updating Schedule..." }
        ScheduleRepository().updateTaskStatesSync(shared.observationFactory, shared.dataRecorder)
        return@withContext Result.success()
    }

    companion object {
        const val WORKER_TAG = "ScheduleUpdateWorker"
    }
}