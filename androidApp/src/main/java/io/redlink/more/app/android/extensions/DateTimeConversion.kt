package io.redlink.more.app.android.extensions

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun Long.jvmLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
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

fun Long.toDate(): Date = Date(this)

fun Date.formattedString(pattern: String = "dd.MM.yyyy"): String {
    val formatter = SimpleDateFormat(pattern)
    return formatter.format(this)
}

fun Long.minuteDiff(other: Long): Long {
    val diff = if(this > other) this - other else other - this
    return diff / 1000 / 60
}