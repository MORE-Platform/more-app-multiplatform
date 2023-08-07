package io.redlink.more.app.android.util

import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.LogLevel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.workers.LogUploadWorker
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.util.ElasticLogHandler
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidLogHandler(private val workManager: WorkManager): ElasticLogHandler {
//    override fun writeLogs(logs: Set<Log>) {
//        //FirebaseCrashlytics.getInstance().recordException(logs.)
//    }

    override fun writeLogs(logs: Set<String>) {
        TODO("Not yet implemented")
    }

}