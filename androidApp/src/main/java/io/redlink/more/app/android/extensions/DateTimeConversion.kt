package io.redlink.more.app.android.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

fun Long.jvmLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(
        this,
        0,
        ZoneId.systemDefault().rules.getOffset(Instant.now())
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