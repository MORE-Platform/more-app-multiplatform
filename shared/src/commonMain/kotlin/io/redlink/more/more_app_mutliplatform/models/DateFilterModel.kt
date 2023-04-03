package io.redlink.more.more_app_mutliplatform.models

import kotlinx.datetime.DateTimeUnit

enum class DateFilterModel(val duration: DateTimeUnit.DateBased?) {
    ENTIRE_TIME(null),
    TODAY_AND_TOMORROW(DateTimeUnit.DAY),
    ONE_WEEK(DateTimeUnit.WEEK),
    ONE_MONTH(DateTimeUnit.MONTH);
}