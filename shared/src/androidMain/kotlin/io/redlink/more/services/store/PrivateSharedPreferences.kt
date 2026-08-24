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
package io.redlink.more.services.store

import android.content.Context
import android.content.SharedPreferences

private const val PRIVATE_SHARED_PREF_FILENAME = "more_app_preferences"

class PrivateSharedPreferences {
    companion object {
        fun create(context: Context): SharedPreferences {
            return createPrivateSharedPreferences(context)
        }

        private fun createPrivateSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                PRIVATE_SHARED_PREF_FILENAME,
                Context.MODE_PRIVATE
            )
        }
    }
}