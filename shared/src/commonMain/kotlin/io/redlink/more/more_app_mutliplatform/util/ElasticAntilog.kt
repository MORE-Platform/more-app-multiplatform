package io.redlink.more.more_app_mutliplatform.util

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.User
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.transformForLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ElasticLogHandler {
    fun writeLogs(logs: Set<String>)
}

class ElasticAntilog(private val elasticLogHandler: ElasticLogHandler): Antilog() {
    private var user: User? = null
    private val logQueue = mutableListOf<Log>()
    private val mutex = Mutex()

    private var sharedWasInit = false


    init {
        CoroutineScope(Job() + Dispatchers.Default).launch {
            StudyRepository().getStudy().cancellable().collect {
                user = it?.let { User(it.participantId, it.participantAlias) }
            }
        }
    }
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            mutex.withLock {
                logQueue.add(Log(priority, message, user = user, tag = tag, throwable?.transformForLog()))
            }
            if (sharedWasInit && (priority == LogLevel.ERROR || priority == LogLevel.ASSERT || logQueue.size >= 100)) {
                val logQueueCopy = mutex.withLock {
                    val copy = logQueue.toSet()
                    logQueue.clear()
                    copy
                }
                elasticLogHandler.writeLogs(logQueueCopy.map { Json.encodeToString(it) }.toSet())
            }
        }
    }
}