package io.redlink.more.more_app_mutliplatform.android.observations

import android.content.Context
import androidx.work.*
import io.redlink.more.more_app_mutliplatform.android.workers.DataUploadWorker
import io.redlink.more.more_app_mutliplatform.database.RealmDatabase
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
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            dataWorker)
        onCompletion(true)
    }

}