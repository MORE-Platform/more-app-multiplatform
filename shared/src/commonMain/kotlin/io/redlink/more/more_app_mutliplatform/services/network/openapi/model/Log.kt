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
package io.redlink.more.more_app_mutliplatform.services.network.openapi.model

import io.github.aakira.napier.LogLevel
import io.redlink.more.more_app_mutliplatform.Platform
import io.redlink.more.more_app_mutliplatform.getPlatform
import kotlinx.datetime.Clock
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
            return LogThrowable(null, throwable?.message)
        }
    }
}

fun Throwable.transformForLog() = LogThrowable.fromSystemThrowable(this)