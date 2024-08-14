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


import platform.Foundation.NSUserDefaults

class UserDefaultsRepository : SharedStorageRepository {
    override fun store(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
    }

    override fun store(key: String, value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, key)
    }

    override fun store(key: String, value: Int) {
        NSUserDefaults.standardUserDefaults.setInteger(value.toLong(), key)
    }

    override fun store(key: String, value: Float) {
        NSUserDefaults.standardUserDefaults.setFloat(value, key)
    }

    override fun store(key: String, value: Double) {
        NSUserDefaults.standardUserDefaults.setDouble(value, key)
    }

    override fun store(key: String, value: Long) {
        NSUserDefaults.standardUserDefaults.setInteger(value, key)
    }

    override fun load(key: String, default: String): String {
        return NSUserDefaults.standardUserDefaults.objectForKey(key) as? String ?: default
    }

    override fun load(key: String, default: Boolean): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(key)
    }

    override fun load(key: String, default: Int): Int {
        return NSUserDefaults.standardUserDefaults.integerForKey(key).toInt()
    }

    override fun load(key: String, default: Float): Float {
        return NSUserDefaults.standardUserDefaults.floatForKey(key)
    }

    override fun load(key: String, default: Double): Double {
        return NSUserDefaults.standardUserDefaults.doubleForKey(key)
    }

    override fun load(key: String, default: Long): Long {
        return NSUserDefaults.standardUserDefaults.integerForKey(key)
    }

    override fun remove(key: String) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
    }
}