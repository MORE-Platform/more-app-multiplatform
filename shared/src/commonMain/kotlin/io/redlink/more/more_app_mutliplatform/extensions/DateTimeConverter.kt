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
package io.redlink.more.more_app_mutliplatform.extensions

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Instant.fromUTCtoCurrent(): Instant {
    val currentZone = TimeZone.currentSystemDefault()
    return this.toLocalDateTime(currentZone).toInstant(currentZone)
}

fun Instant.localDateTime(): LocalDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.time(): Long =
    this.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).epochSeconds

fun Long.toLocalDateTime(): LocalDateTime = Instant.fromEpochSeconds(this).localDateTime()

fun Long.toLocalDate() = toLocalDateTime().date
