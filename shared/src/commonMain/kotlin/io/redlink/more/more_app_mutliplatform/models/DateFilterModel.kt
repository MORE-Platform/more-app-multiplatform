package io.redlink.more.more_app_mutliplatform.models

import kotlinx.datetime.DateTimeUnit

enum class DateFilterModel(private val duration: DateTimeUnit.DateBased) {
    TODAY_AND_TOMORROW(DateTimeUnit.DAY),
    ONE_WEEK(DateTimeUnit.WEEK),
    ONE_MONTH(DateTimeUnit.MONTH);

    fun getDuration(): DateTimeUnit.DateBased = this.duration
}