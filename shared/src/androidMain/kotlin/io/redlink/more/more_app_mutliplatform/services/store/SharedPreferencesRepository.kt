package io.redlink.more.more_app_mutliplatform.services.store

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesRepository(context: Context): SharedStorageRepository {
    private var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(context)

    override fun store(key: String, value: List<String>) {
        sharedPreferences
            .edit()
            ?.putStringSet(key, value.toSet())
            ?.apply()
    }

    override fun store(key: String, value: String) {
        sharedPreferences
            .edit()
            ?.putString(key, value)
            ?.apply()
    }

    override fun store(key: String, value: Boolean) {
        sharedPreferences
            .edit()
            ?.putBoolean(key, value)
            ?.apply()
    }

    override fun store(key: String, value: Int) {
        sharedPreferences
            .edit()
            ?.putInt(key, value)
            ?.apply()
    }

    override fun store(key: String, value: Float) {
        sharedPreferences
            .edit()
            ?.putFloat(key, value)
            ?.apply()
    }

    override fun store(key: String, value: Double) {
        sharedPreferences
            .edit()
            ?.putLong(key, value.toRawBits())
            ?.apply()
    }

    override fun load(key: String, default: List<String>): List<String> {
        val defaultValue = emptySet<String>()
        return sharedPreferences.getStringSet(key, defaultValue)?.toList() ?: emptyList()
    }

    override fun load(key: String, default: String): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    override fun load(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun load(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    override fun load(key: String, default: Float): Float {
        return sharedPreferences.getFloat(key, default)
    }

    override fun load(key: String, default: Double): Double {
        return sharedPreferences.getLong(key, default.toRawBits()).toDouble()
    }

    override fun remove(key: String) {
        sharedPreferences.edit()?.remove(key)?.apply()
    }
}