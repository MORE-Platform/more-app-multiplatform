package io.redlink.more.more_app_mutliplatform.services.store
//
//import android.content.SharedPreferences
//
//
class SharedPreferencesRepository(): SharedStorageRepository {
    override fun storeSecure(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun loadSecure(key: String, default: String): String {
        TODO("Not yet implemented")
    }

    override fun store(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun store(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun store(key: String, value: Int) {
        TODO("Not yet implemented")
    }

    override fun store(key: String, value: Float) {
        TODO("Not yet implemented")
    }

    override fun store(key: String, value: Double) {
        TODO("Not yet implemented")
    }

    override fun load(key: String, default: String): String {
        TODO("Not yet implemented")
    }

    override fun load(key: String, default: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun load(key: String, default: Int): Int {
        TODO("Not yet implemented")
    }

    override fun load(key: String, default: Float): Float {
        TODO("Not yet implemented")
    }

    override fun load(key: String, default: Double): Double {
        TODO("Not yet implemented")
    }
}
//
actual fun getSharedStorageRepository(): SharedStorageRepository = SharedPreferencesRepository()