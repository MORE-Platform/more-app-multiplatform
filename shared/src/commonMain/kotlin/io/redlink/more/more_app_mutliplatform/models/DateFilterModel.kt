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
package io.redlink.more.more_app_mutliplatform.models

import kotlinx.datetime.DateTimeUnit

data class DateFilter(
    val describing: String,
    val number: Int = 1,
    val dateBased: DateTimeUnit.DateBased?,
    val sortIndex: Int,
    var selected: Boolean = false
) {
    fun toEnum(): DateFilterModel? {
        return DateFilterModel.values()
            .firstOrNull { describing == it.toString() }
    }
}

enum class DateFilterModel(
    val number: Int = 1,
    val dateBased: DateTimeUnit.DateBased?,
    val sortIndex: Int
) {
    ENTIRE_TIME(1, null, 0),
    TODAY_AND_TOMORROW(1, DateTimeUnit.DAY, 1),
    ONE_WEEK(1, DateTimeUnit.WEEK, 2),
    ONE_MONTH(1, DateTimeUnit.MONTH, 3);

    fun asDataClass() = DateFilter(toString() ,number, dateBased, sortIndex)

    companion object {
        fun asDataClassList() = DateFilterModel.values().map { it.asDataClass() }
    }
}