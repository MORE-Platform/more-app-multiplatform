package io.redlink.more.app.android.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogUploadWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    private val sharedPreferences = SharedPreferencesRepository(applicationContext)
    private val credentialRepository: CredentialRepository =
        MoreApplication.shared?.credentialRepository ?: CredentialRepository(sharedPreferences)
    private val networkService = MoreApplication.shared?.networkService ?: NetworkService(
        EndpointRepository(sharedPreferences), credentialRepository
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            inputData.getString(WORKER_DATA)?.let { data ->
                val logList = deserializeData(data)
                if (logList.isNotEmpty()) {
                    if (networkService.sendLogs(logList)) {
                        Result.success()
                    } else if (runAttemptCount > 3) {
                        Result.failure()
                    } else {
                        Result.retry()
                    }
                } else {
                    Result.failure()
                }
            } ?: Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORKER_TAG = "LogUploadWorker"
        private const val WORKER_DATA = "logData"

        fun scheduleWorker(workManager: WorkManager, logs: List<Log>) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                .build()
            val data = Data.Builder()
                .putString(WORKER_DATA, serializeData(logs))
                .build()
            val worker = OneTimeWorkRequestBuilder<LogUploadWorker>()
                .setConstraints(constraints)
                .addTag(WORKER_TAG)
                .setInputData(data)
                .build()
            workManager.enqueue(worker)
        }

        private fun serializeData(logs: List<Log>) = Gson().toJson(logs)

        private fun deserializeData(data: String): List<Log> {
            val listType = object : TypeToken<List<Log>>() {}.type
            return try {
                Gson().fromJson(data, listType)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
