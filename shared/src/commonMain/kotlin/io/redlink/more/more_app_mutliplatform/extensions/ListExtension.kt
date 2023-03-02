package io.redlink.more.more_app_mutliplatform.extensions

import io.redlink.more.more_app_mutliplatform.models.ScheduleModel

fun List<ScheduleModel>.toDateMap() = this.map { it.start.localDateTime().date }.toSet()
    .associateBy(
        { it },
        { localDate -> this.filter { it.start.localDateTime().date == localDate } })