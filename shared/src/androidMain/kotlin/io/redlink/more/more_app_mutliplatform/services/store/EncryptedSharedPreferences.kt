package io.redlink.more.more_app_mutliplatform.services.store

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

private const val ENCRYPT_SHARED_PREF_FILENAME = "credentials_file"
class EncryptedSharedPreference {
    companion object {
        fun create(context: Context): SharedPreferences {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
            return EncryptedSharedPreferences.create(
                ENCRYPT_SHARED_PREF_FILENAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}