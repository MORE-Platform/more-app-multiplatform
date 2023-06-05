package io.redlink.more.app.android.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationDataRepository
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DataUploadWorker"

/**
 * The Worker, which tries to upload given databulks. It receives a bulk of data ids, queries them from the SQLite and sends them to the DSB. Then it deletes those ids on a successful return.
 */
class DataUploadWorker (
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private val workManager = WorkManager.getInstance(applicationContext)
    private val sharedPreferences = SharedPreferencesRepository(applicationContext)
    private val credentialRepository: CredentialRepository = MoreApplication.shared?.credentialRepository ?: CredentialRepository(sharedPreferences)
    private val networkService = MoreApplication.shared?.networkService ?: NetworkService(EndpointRepository(sharedPreferences), credentialRepository)
    private var stopped = false
    private val observationDataRepository = ObservationDataRepository()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        if (!credentialRepository.hasCredentials()) return@withContext Result.failure()
        try {
            Log.d(TAG, "Worker started!")
            return@withContext observationDataRepository.allAsBulk()?.let { bulk ->
                if (bulk.dataPoints.isNotEmpty()) {
                    return@withContext uploadDataBulk(bulk).apply {
                        observationDataRepository.close()
                    }
                }
                Result.success()
            } ?: Result.failure()
        } catch (err: Exception) {
            Log.e(TAG, err.stackTraceToString())
            if (isStopped) {
                stopped = isStopped
                workManager.cancelAllWork()
            }
            Result.failure()
        }
    }

    private suspend fun uploadDataBulk(bulk: DataBulk): Result {
        val (ids, error) = networkService.sendData(bulk)
        return if (error != null) {
            Napier.e { error.message }
            if (stopped || runAttemptCount > MAX_WORKER_RETRY) {
                Result.failure()
            }
            Result.retry()
        } else {
            Napier.d { "Deleting observation data..." }
            observationDataRepository.deleteAllWithId(ids)
            Result.success()
        }
    }

    companion object {
        const val WORKER_TAG = "DATA_UPLOAD_WORKER"
        const val MAX_WORKER_RETRY = 5
    }

}