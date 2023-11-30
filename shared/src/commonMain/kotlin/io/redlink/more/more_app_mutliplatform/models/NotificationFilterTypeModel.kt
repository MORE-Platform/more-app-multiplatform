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

enum class NotificationFilterTypeModel(val type: String, val sortIndex: Int) {
    ALL("All", 0),
    UNREAD("Unread", 1),
    IMPORTANT("Important", 2);

    companion object {
        fun createModel(type: String): NotificationFilterTypeModel? {
            return when(type) {
                ALL.type -> ALL
                UNREAD.type -> UNREAD
                IMPORTANT.type -> IMPORTANT
                else -> { null }
            }
        }
    }
}