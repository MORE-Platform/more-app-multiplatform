package io.redlink.more.app.android.util

import androidx.work.WorkManager
import io.github.aakira.napier.LogLevel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.workers.LogUploadWorker
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.util.ElasticLogHandler
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidLogHandler(private val workManager: WorkManager): ElasticLogHandler {
    private val logQueue = mutableListOf<Log>()
    private val mutex = Mutex()
    override fun appendNewLog(log: Log) {
        Scope.launch {
            //android.util.Log.i("AndroidLogHandler", "New Log added for Elastic")
            mutex.withLock {
                logQueue.add(log)
            }
            if (MoreApplication.shared != null && (log.priority == LogLevel.ERROR || log.priority == LogLevel.ASSERT || logQueue.size >= 10)) {
                //android.util.Log.i("AndroidLogHandler", "Sending data to elastic")
                releaseDataToBackend()
            }
        }
    }

    private suspend fun releaseDataToBackend() {
        if (logQueue.isNotEmpty()) {
            var queueCopy: List<Log>
            mutex.withLock {
                queueCopy = logQueue.toList()
                logQueue.clear()
            }
            if (queueCopy.isNotEmpty()) {
                LogUploadWorker.scheduleWorker(workManager, queueCopy)
            }
        }
    }
}