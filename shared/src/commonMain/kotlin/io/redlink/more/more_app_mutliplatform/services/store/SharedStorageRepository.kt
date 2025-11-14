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

interface SharedStorageRepository {
    fun store(key: String, value: String)
    fun store(key: String, value: Boolean)
    fun store(key: String, value: Int)
    fun store(key: String, value: Float)
    fun store(key: String, value: Double)
    fun store(key: String, value: Long)

    fun load(key: String, default: String): String
    fun load(key: String, default: Boolean): Boolean
    fun load(key: String, default: Int): Int
    fun load(key: String, default: Float): Float
    fun load(key: String, default: Double): Double
    fun load(key: String, default: Long): Long

    fun remove(key: String)
}