package io.redlink.more.more_app_mutliplatform.services.store

interface SharedStorageRepository {
    fun store(key: String, value: String)
    fun store(key: String, value: Boolean)
    fun store(key: String, value: Int)
    fun store(key: String, value: Float)
    fun store(key: String, value: Double)

    fun load(key: String, default: String): String
    fun load(key: String, default: Boolean): Boolean
    fun load(key: String, default: Int): Int
    fun load(key: String, default: Float): Float
    fun load(key: String, default: Double): Double

    fun remove(key: String)
}