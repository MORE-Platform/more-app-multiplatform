package io.redlink.more.more_app_mutliplatform.services.network.openapi.model

import io.github.aakira.napier.LogLevel
import io.redlink.more.more_app_mutliplatform.Platform
import io.redlink.more.more_app_mutliplatform.getPlatform
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Log(
    val priority: LogLevel,
    val message: String?,
    val user: User? = null,
    val tag: String? = null,
    val throwable: LogThrowable? = null,
){
    val timestamp: String = Clock.System.now().toString()
    val platform: Platform = getPlatform()
}

@Serializable
data class User(
    val userId: Int?,
    val alias: String?
)

@Serializable
data class LogThrowable(
    val cause: LogThrowable?,
    val message: String?
) {
    companion object {
        fun fromSystemThrowable(throwable: Throwable?): LogThrowable {
            return LogThrowable(fromSystemThrowable(throwable?.cause), throwable?.message)
        }
    }
}

fun Throwable.transformForLog() = LogThrowable.fromSystemThrowable(this)