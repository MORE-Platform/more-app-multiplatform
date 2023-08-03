package io.redlink.more.more_app_mutliplatform.util

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.User
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.transformForLog
import kotlinx.coroutines.flow.cancellable

interface ElasticLogHandler {
    fun appendNewLog(log: Log)
}

class ElasticAntilog(private val elasticLogHandler: ElasticLogHandler): Antilog() {
    private var user: User? = null

    init {
        Scope.launch {
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
        elasticLogHandler.appendNewLog(Log(priority, message, user = user, tag = tag, throwable?.transformForLog()))
    }
}