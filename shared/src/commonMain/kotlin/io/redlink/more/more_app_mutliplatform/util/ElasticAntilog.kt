package io.redlink.more.more_app_mutliplatform.util

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.ktor.util.logging.Logger
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Log
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.User
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.transformForLog
import kotlinx.coroutines.flow.cancellable

interface ElasticLogHandler {
    fun appendNewLog(log: Log)
}

class ElasticAntilog(private val elasticLogHandler: ElasticLogHandler): Antilog(), Logger {
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

    override fun debug(message: String) {
        elasticLogHandler.appendNewLog(Log(LogLevel.DEBUG, message, user = user, "KtorClient"))
    }

    override fun debug(message: String, cause: Throwable) {
        elasticLogHandler.appendNewLog(Log(LogLevel.DEBUG, message, user = user, "KtorClient", throwable = cause.transformForLog()))
    }

    override fun error(message: String) {
        elasticLogHandler.appendNewLog(Log(LogLevel.ERROR, message, user = user, "KtorClient"))
    }

    override fun error(message: String, cause: Throwable) {
        elasticLogHandler.appendNewLog(Log(LogLevel.ERROR, message, user = user, "KtorClient", throwable = cause.transformForLog()))
    }

    override fun info(message: String) {
        elasticLogHandler.appendNewLog(Log(LogLevel.INFO, message, user = user, "KtorClient"))
    }

    override fun info(message: String, cause: Throwable) {
        elasticLogHandler.appendNewLog(Log(LogLevel.INFO, message, user = user, "KtorClient", throwable = cause.transformForLog()))
    }

    override fun trace(message: String) {
        elasticLogHandler.appendNewLog(Log(LogLevel.ASSERT, message, user = user, "KtorClient"))
    }

    override fun trace(message: String, cause: Throwable) {
        elasticLogHandler.appendNewLog(Log(LogLevel.ASSERT, message, user = user, "KtorClient", throwable = cause.transformForLog()))
    }

    override fun warn(message: String) {
        elasticLogHandler.appendNewLog(Log(LogLevel.WARNING, message, user = user, "KtorClient"))
    }

    override fun warn(message: String, cause: Throwable) {
        elasticLogHandler.appendNewLog(Log(LogLevel.WARNING, message, user = user, "KtorClient", throwable = cause.transformForLog()))
    }
}