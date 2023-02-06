package io.redlink.more.more_app_mutliplatform.services.store

import platform.Foundation.NSUserDefaults
import platform.darwin.NSInteger


class UserDefaultsRepository: SharedStorageRepository {
    override fun store(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
    }

    override fun store(key: String, value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, key)
    }

    override fun store(key: String, value: Int) {
        NSUserDefaults.standardUserDefaults.setInteger(value as NSInteger, key)
    }

    override fun store(key: String, value: Float) {
        NSUserDefaults.standardUserDefaults.setFloat(value, key)
    }

    override fun store(key: String, value: Double) {
        NSUserDefaults.standardUserDefaults.setDouble(value, key)
    }

    override fun load(key: String, default: String): String {
        return NSUserDefaults.standardUserDefaults.objectForKey(key) as? String ?: default
    }

    override fun load(key: String, default: Boolean): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(key) ?: default
    }

    override fun load(key: String, default: Int): Int {
        return NSUserDefaults.standardUserDefaults.integerForKey(key).toInt() ?: default
    }

    override fun load(key: String, default: Float): Float {
        return NSUserDefaults.standardUserDefaults.floatForKey(key) ?: default
    }

    override fun load(key: String, default: Double): Double {
        return NSUserDefaults.standardUserDefaults.doubleForKey(key) ?: default
    }
}

actual fun getSharedStorageRepository(): SharedStorageRepository = UserDefaultsRepository()