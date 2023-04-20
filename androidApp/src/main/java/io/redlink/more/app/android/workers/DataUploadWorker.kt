package io.redlink.more.app.android.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
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
    private val workerConstraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    private val workManager = WorkManager.getInstance(applicationContext)
    private val credentialRepository: CredentialRepository
    private val networkService: NetworkService
    private var stopped = false
    private val observationDataRepository = ObservationDataRepository()

    init {
        val sharedPreferences = SharedPreferencesRepository(applicationContext)
        credentialRepository = CredentialRepository(sharedPreferences)
        networkService = NetworkService(EndpointRepository(sharedPreferences), credentialRepository)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!credentialRepository.hasCredentials()) return@withContext Result.failure()
        try {
            Log.d(TAG, "Worker started!")
            return@withContext observationDataRepository.allAsBulk()?.let {
                val size = it.dataPoints.size
                if (size > 0) {
                    if (size > MAX_NUMBER_OF_DATA_POINTS) {
                        enqueueNewWorker()
                        return@let uploadDataBulk(DataBulk(it.bulkId, dataPoints = it.dataPoints.chunked(
                            MAX_NUMBER_OF_DATA_POINTS
                        ).first()))
                    }
                    return@let uploadDataBulk(it)
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
            Log.e(TAG, error.message)
            if (stopped || runAttemptCount > MAX_WORKER_RETRY) {
                Result.failure()
            }
            Result.retry()
        } else {
            observationDataRepository.deleteAllWithId(ids)
            Result.success()
        }
    }

    private fun enqueueNewWorker() {
        val dataWorker = OneTimeWorkRequestBuilder<DataUploadWorker>()
            .setConstraints(workerConstraints)
            .build()
        workManager.enqueueUniqueWork(
            WORKER_TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            dataWorker)
    }

    companion object {
        const val WORKER_TAG = "DATA_UPLOAD_WORKER"
        const val MAX_NUMBER_OF_DATA_POINTS = 1000
        const val MAX_WORKER_RETRY = 5
    }

}