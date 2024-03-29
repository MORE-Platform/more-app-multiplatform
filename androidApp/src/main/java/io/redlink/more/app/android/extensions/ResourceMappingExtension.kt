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

import io.redlink.more.app.android.R
import io.redlink.more.more_app_mutliplatform.models.DateFilterModel

fun String.formatDateFilterString(): String {
    return when(this) {
        DateFilterModel.TODAY_AND_TOMORROW.toString() -> stringResource(R.string.more_filter_today_tomorrow)
        DateFilterModel.ONE_WEEK.toString() -> stringResource(R.string.more_filter_week)
        DateFilterModel.ONE_MONTH.toString() -> stringResource(R.string.more_filter_month)
        else -> stringResource(R.string.more_filter_entire_time)
    }

}

fun String.formatObservationTypeString(): String {
    return when(this) {
        "question-observation" -> stringResource(R.string.more_filter_question)
        "gps-mobile-observation" -> stringResource(R.string.more_filter_gps)
        "acc-mobile-observation" -> stringResource(R.string.more_filter_accelerometer)
        "polar-verity-observation" -> stringResource(R.string.more_filter_polar)
        else -> "Unknown Type Filter"
    }
}