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
package io.redlink.more.app.android.observations

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.workers.DataUploadWorker
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager

class AndroidObservationDataManager(context: Context) : ObservationDataManager() {
    private val workManager = WorkManager.getInstance(context)
    private val workerConstraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override fun sendData(onCompletion: (Boolean) -> Unit) {
        val dataWorker = OneTimeWorkRequestBuilder<DataUploadWorker>()
            .setConstraints(workerConstraints)
            .build()
        workManager.enqueueUniqueWork(
            DataUploadWorker.WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            dataWorker)
        onCompletion(true)
    }

}