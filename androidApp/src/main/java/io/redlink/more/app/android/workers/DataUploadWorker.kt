/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
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
        Napier.i { "Starting DataUploadWorker doWork()" }

        if (!credentialRepository.hasCredentials()) {
            Napier.i { "No credentials found, DataUploadWorker failure" }
            return@withContext Result.failure()
        }

        try {
            Napier.i { "Worker started!" }
            return@withContext observationDataRepository.allAsBulk()?.let { bulk ->
                if (bulk.dataPoints.isNotEmpty()) {
                    return@withContext uploadDataBulk(bulk).apply {
                        observationDataRepository.close()
                    }
                }
                Napier.i { "No data points found, DataUploadWorker success" }
                Result.success()
            } ?: Result.failure()
        } catch (err: Exception) {
            Napier.e(throwable = err, message =  "Exception in DataUploadWorker")
            if (isStopped) {
                stopped = isStopped
                workManager.cancelAllWork()
                Napier.i { "WorkManager cancelled all work due to stopped state" }
            }
            Result.failure()
        }
    }

    private suspend fun uploadDataBulk(bulk: DataBulk): Result {
        val (ids, error) = networkService.sendData(bulk)
        return if (error != null) {
            Napier.e { "Error while sending data: ${error.message}" }
            if (stopped || runAttemptCount > MAX_WORKER_RETRY) {
                Napier.i { "Worker stopped or max retry attempts reached, failure" }
                Result.failure()
            }
            Napier.i { "Worker retry" }
            Result.retry()
        } else {
            Napier.i { "Deleting observation data..." }
            observationDataRepository.deleteAllWithId(ids)
            Napier.i { "Deleted ${ids.size} observation data points, success" }
            Result.success()
        }
    }

    companion object {
        const val WORKER_TAG = "DATA_UPLOAD_WORKER"
        const val MAX_WORKER_RETRY = 5
    }

}