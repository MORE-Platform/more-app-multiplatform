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
package io.redlink.more.app.android.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun Long.jvmLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(
        this,
        0,
        ZoneId.systemDefault().rules.getOffset(Instant.ofEpochSecond(this))
    )
}

fun Long.jvmLocalDateTimeFromMilliseconds(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(
        this / 1000,
        0,
        ZoneId.systemDefault().rules.getOffset(Instant.ofEpochSecond(this / 1000))  // convert milliseconds to seconds
    )
}

fun Long.jvmLocalDate(): LocalDate = this.jvmLocalDateTime().toLocalDate()

fun LocalDate.formattedString(pattern: String = "dd.MM.yyyy"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}

fun LocalDateTime.formattedString(pattern: String = "dd.MM.yyyy"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}

fun Long.toDate(): Date = Date.from(Instant.ofEpochSecond(this))

@SuppressLint("SimpleDateFormat")
fun Date.formattedString(pattern: String = "dd.MM.yyyy"): String {
    val formatter = SimpleDateFormat(pattern)
    return formatter.format(this)
}

fun Long.minuteDiff(other: Long): Long {
    val diff = if (this > other) this - other else other - this
    return diff / 1000 / 60
}