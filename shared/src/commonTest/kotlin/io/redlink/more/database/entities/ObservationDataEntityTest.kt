/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.entities

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservationDataEntityTest {

    @Test
    fun testFromDataWithSeconds() {
        val seconds = 1714925000L // around May 2024
        val entity = ObservationDataEntity.fromData(mapOf("test" to "value"), seconds)
        assertEquals(seconds * 1000, entity.timestamp)
    }

    @Test
    fun testFromDataWithMilliseconds() {
        val ms = 1714925000000L // around May 2024 in ms
        val entity = ObservationDataEntity.fromData(mapOf("test" to "value"), ms)
        assertEquals(ms, entity.timestamp)
    }

    @Test
    fun testFromDataWithDefault() {
        val before = Clock.System.now().toEpochMilliseconds()
        val entity = ObservationDataEntity.fromData(mapOf("test" to "value"))
        val after = Clock.System.now().toEpochMilliseconds()
        assertTrue(entity.timestamp in before..after)
    }

    @Test
    fun testFromDataWithZero() {
        val before = Clock.System.now().toEpochMilliseconds()
        val entity = ObservationDataEntity.fromData(mapOf("test" to "value"), 0)
        val after = Clock.System.now().toEpochMilliseconds()
        assertTrue(entity.timestamp in before..after)
    }
}
