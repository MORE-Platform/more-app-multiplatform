package io.redlink.more.more_app_mutliplatform.extensions

import io.realm.kotlin.types.RealmInstant
import kotlinx.datetime.*

fun RealmInstant.toInstant(): Instant {
    val sec: Long = this.epochSeconds
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0) {
        Instant.fromEpochSeconds(sec, nano.toLong())
    } else {
        Instant.fromEpochSeconds(sec - 1, 1_000_000 + nano.toLong())
    }
}

fun Instant.toRealmInstant(): RealmInstant {
    val sec: Long = this.epochSeconds
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0) {
        RealmInstant.from(sec, nano)
    } else {
        RealmInstant.from(sec + 1, -1_000_000 + nano)
    }
}

fun Instant.fromUTCtoCurrent(): Instant {
    val currentZone = TimeZone.currentSystemDefault()
    return this.toLocalDateTime(currentZone).toInstant(currentZone)
}

fun Instant.localDateTime(): LocalDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.time(): Long =
    this.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).epochSeconds

fun Long.toLocalDateTime(): LocalDateTime = Instant.fromEpochMilliseconds(this).localDateTime()

fun Long.toLocalDate() = toLocalDateTime().date
