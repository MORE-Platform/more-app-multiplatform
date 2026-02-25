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
package io.redlink.umm.blendedcare.app.android.observations

import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.workers.DataUploadWorker
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.observations.ObservationDataManager
import io.redlink.umm.participant.scopes.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

class AndroidObservationDataManager(context: Context, repository: MainRepository) :
    ObservationDataManager(repository) {
    private val workManager: WorkManager? = try {
        WorkManager.getInstance(context)
    } catch (e: IllegalStateException) {
        Napier.e(tag = "AndroidObservationDataManager::workManager::init") { "Error init WorkManager: ${e.message}" }
        null
    }
    private val workerConstraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override fun sendData(immediately: Boolean, onCompletion: (Boolean) -> Unit) {
        Scope.launch {
            if (!immediately && workManager != null) {
                onCompletion(tryWorkManagerThenFallback())
            } else {
                Napier.w { "WorkManager not available, falling back to direct upload..." }
                onCompletion(directUploadFallback())
            }
        }
    }

    private suspend fun tryWorkManagerThenFallback(): Boolean {
        return workManager?.let { workManager ->
            val request = OneTimeWorkRequestBuilder<DataUploadWorker>()
                .setConstraints(workerConstraints)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }
                }
                .addTag(DataUploadWorker.WORKER_TAG)
                .build()

            workManager.enqueueUniqueWork(
                DataUploadWorker.WORKER_TAG,
                ExistingWorkPolicy.KEEP,
                request
            )

            val result = waitForWorkOrTimeout(request.id, timeoutMs = 25_000L)
            Napier.i { "WorkManager work with id ${request.id} finished with state $result" }
            when (result) {
                WorkInfo.State.SUCCEEDED -> true
                WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> directUploadFallback()
                else -> directUploadFallback()
            }
        } ?: false
    }

    private suspend fun waitForWorkOrTimeout(id: UUID, timeoutMs: Long): WorkInfo.State {
        Napier.i { "Waiting for WorkManager work with id $id to finish (max $timeoutMs ms)..." }
        return withTimeoutOrNull(timeoutMs) {
            workManager?.getWorkInfoByIdFlow(id)?.collect { info ->
                info?.let { workInfo ->
                    Napier.i { "WorkManager work with id $id is in state ${workInfo.state}" }
                    if (workInfo.state.isFinished) return@collect
                }
            }
            WorkInfo.State.ENQUEUED
        } ?: WorkInfo.State.ENQUEUED
    }

    private suspend fun directUploadFallback(
        maxAttempts: Int = 3,
        baseDelayMs: Long = 1_000L
    ): Boolean = withContext(Dispatchers.IO) {
        val networkService = BlendedCareApplication.Companion.shared?.networkService
        if (networkService == null) {
            Napier.e(tag = "AndroidObservationDataManager::directUploadFallback") {
                "NetworkService not available"
            }
            return@withContext false
        }
        Napier.i { "Sending data via fallback method..." }
        var attemptCount = 0
        while (attemptCount < maxAttempts && isConnected()) {
            dataBulk()?.let { bulk ->
                if (bulk.dataPoints.isNotEmpty()) {
                    val (ids, error) = networkService.sendData(bulk)
                    if (error != null) {
                        Napier.e { "Error sending data: $error" }
                        attemptCount++
                        delay(baseDelayMs * attemptCount)
                        continue
                    } else {
                        Napier.i { "Successfully sent ${ids.size} data points! Deleting data from local database..." }
                        deleteAll(ids)
                        Napier.i { "Successfully deleted ${ids.size} data points!" }
                        return@withContext true
                    }
                }
            }
        }
        Napier.e { "Max attempts ($maxAttempts) reached, no data points sent!" }
        return@withContext false
    }
}