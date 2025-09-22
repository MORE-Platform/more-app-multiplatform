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

package io.redlink.more.more_app_mutliplatform.services.store

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.aakira.napier.Napier
import java.io.File
import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException

private const val ENCRYPT_SHARED_PREF_FILENAME = "credentials_file"
private const val TAG = "EncryptedSharedPrefs"

// TODO: Remove encrypted shared preferences after all active devices updated to this version
class EncryptedSharedPreferences {
    companion object {
        fun create(context: Context): SharedPreferences {
            return try {
                createEncryptedSharedPreferences(context)
            } catch (exception: AEADBadTagException) {
                Napier.w(
                    exception,
                    tag = TAG
                ) { "Failed to create EncryptedSharedPreferences, attempting recovery" }
                handleEncryptionFailure(context, exception)
            }
        }

        private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()


            return EncryptedSharedPreferences.create(
                context,
                ENCRYPT_SHARED_PREF_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        private fun handleEncryptionFailure(
            context: Context,
            exception: Exception
        ): SharedPreferences = when (exception) {
            is AEADBadTagException, is GeneralSecurityException -> {
                Napier.w(tag = TAG) { "Encryption/decryption error detected, clearing corrupted data" }
                clearCorruptedData(context)

                try {
                    createEncryptedSharedPreferences(context)
                } catch (retryException: Exception) {
                    Napier.e(
                        retryException,
                        tag = TAG
                    ) { "Failed to create EncryptedSharedPreferences after recovery, falling back to regular SharedPreferences" }
                    createFallbackSharedPreferences(context)
                }
            }

            else -> {
                Napier.e(
                    exception,
                    tag = TAG
                ) { "Unexpected error creating EncryptedSharedPreferences, falling back to regular SharedPreferences" }
                createFallbackSharedPreferences(context)
            }
        }

        private fun clearCorruptedData(context: Context) {
            try {
                val sharedPrefsFile =
                    context.getSharedPreferences(ENCRYPT_SHARED_PREF_FILENAME, Context.MODE_PRIVATE)
                sharedPrefsFile.edit().clear().apply()

                val prefsDir = context.filesDir.parent?.let { "$it/shared_prefs/" }
                    ?: throw IllegalStateException("Failed to determine shared_prefs directory")
                val prefsFile = File(prefsDir, "$ENCRYPT_SHARED_PREF_FILENAME.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }

                Napier.i(tag = TAG) { "Successfully cleared corrupted encrypted preferences data" }
            } catch (clearException: Exception) {
                Napier.e(clearException, tag = TAG) { "Failed to clear corrupted data" }
            }
        }

        private fun createFallbackSharedPreferences(context: Context): SharedPreferences {
            Napier.w(tag = TAG) { "Using fallback unencrypted SharedPreferences - consider implementing additional security measures" }
            return context.getSharedPreferences(
                "${ENCRYPT_SHARED_PREF_FILENAME}_fallback",
                Context.MODE_PRIVATE
            )
        }
    }
}