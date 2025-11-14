package io.redlink.more.more_app_mutliplatform.services.store

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