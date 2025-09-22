package io.redlink.more.more_app_mutliplatform.services.store

import android.content.Context
import android.content.SharedPreferences
import io.github.aakira.napier.Napier

private const val PRIVATE_SHARED_PREF_FILENAME = "more_app_preferences"
private const val ENCRYPT_SHARED_PREF_FILENAME = "credentials_file"
private const val ENCRYPT_SHARED_PREF_FALLBACK_FILENAME = "${ENCRYPT_SHARED_PREF_FILENAME}_fallback"
private const val TAG = "PrivateSharedPrefs"

// This is a replacement key value store for the encrypted shared preferences, as those would crash the app if the key was not right anymore, like after an app reinstall, phone restore or phone transfer where the data were backed up, but the key did not.
// TODO: Remove migration script after all active devices updated to this version
class PrivateSharedPreferences {
    companion object {
        fun create(context: Context): SharedPreferences {
            val privatePrefs = createPrivateSharedPreferences(context)

            if (shouldMigrateFromEncrypted(context)) {
                migrateFromEncryptedPreferences(context, privatePrefs)
            }

            return privatePrefs
        }

        private fun createPrivateSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                PRIVATE_SHARED_PREF_FILENAME,
                Context.MODE_PRIVATE
            )
        }

        private fun shouldMigrateFromEncrypted(context: Context): Boolean {
            val encryptedPrefsFile =
                context.getSharedPreferences(ENCRYPT_SHARED_PREF_FILENAME, Context.MODE_PRIVATE)
            val fallbackPrefsFile = context.getSharedPreferences(
                ENCRYPT_SHARED_PREF_FALLBACK_FILENAME,
                Context.MODE_PRIVATE
            )

            return encryptedPrefsFile.all.isNotEmpty() || fallbackPrefsFile.all.isNotEmpty()
        }

        private fun migrateFromEncryptedPreferences(
            context: Context,
            privatePrefs: SharedPreferences
        ) {
            try {
                Napier.i(tag = TAG) { "Starting migration from encrypted preferences" }

                val encryptedPrefs = try {
                    EncryptedSharedPreferences.create(context)
                } catch (e: Exception) {
                    Napier.w(
                        e,
                        tag = TAG
                    ) { "Failed to access encrypted preferences, trying fallback" }
                    context.getSharedPreferences(
                        ENCRYPT_SHARED_PREF_FALLBACK_FILENAME,
                        Context.MODE_PRIVATE
                    )
                }

                val editor = privatePrefs.edit()

                for ((key, value) in encryptedPrefs.all) {
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Float -> editor.putFloat(key, value)
                        is Long -> editor.putLong(key, value)
                        else -> Napier.w(tag = TAG) { "Unknown type for key: $key, value: $value" }
                    }
                }

                editor.apply()

                Napier.i(tag = TAG) { "Migration completed successfully" }

                clearOldPreferences(context)
            } catch (e: Exception) {
                Napier.e(e, tag = TAG) { "Error during migration from encrypted preferences" }
            }
        }

        private fun clearOldPreferences(context: Context) {
            try {
                context.getSharedPreferences(ENCRYPT_SHARED_PREF_FILENAME, Context.MODE_PRIVATE)
                    .edit().clear().apply()

                context.getSharedPreferences(
                    ENCRYPT_SHARED_PREF_FALLBACK_FILENAME,
                    Context.MODE_PRIVATE
                )
                    .edit().clear().apply()

                Napier.i(tag = TAG) { "Old preferences cleared successfully" }
            } catch (e: Exception) {
                Napier.w(e, tag = TAG) { "Failed to clear old preferences" }
            }
        }
    }
}