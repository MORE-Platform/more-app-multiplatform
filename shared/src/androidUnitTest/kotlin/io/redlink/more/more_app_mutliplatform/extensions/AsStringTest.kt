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
package io.redlink.more.more_app_mutliplatform.extensions

import com.google.gson.Gson
import kotlin.reflect.KClass
import kotlin.test.*

class AsStringTest {

    @Test
    fun testPrimitives() {
        fun <T: Any> testPrimitive(value: T, type: KClass<T>) {
            assertEquals(value, Gson().fromJson(value.asString(), type.java),
                "Checking $type: $value")
        }

        testPrimitive(5, Int::class)
        testPrimitive(Int.MIN_VALUE, Int::class)
        testPrimitive(Int.MAX_VALUE, Int::class)

        testPrimitive(123L, Long::class)
        testPrimitive(5, Long::class)
        testPrimitive(Long.MIN_VALUE, Long::class)
        testPrimitive(Long.MAX_VALUE, Long::class)

        testPrimitive(1.2F, Float::class)
        testPrimitive(1F, Float::class)
        testPrimitive(-1.002F, Float::class)

        testPrimitive(1.2, Double::class)
        testPrimitive(0.0, Double::class)
        testPrimitive(-1.002, Double::class)

        testPrimitive(true, Boolean::class)
        testPrimitive(false, Boolean::class)

        testPrimitive("This is a String", String::class)
    }

    @Test
    fun testArray() {
        fun <T: Any> test(array: Array<T>, type: KClass<Array<T>>) {
            assertContentEquals(array, Gson().fromJson(array.asString(), type.java),
                "Checking $array")
        }

        test(arrayOf(1,2,3), Array<Int>::class)
        test(arrayOf(1.1,2.0,3.0), Array<Double>::class)
        test(arrayOf("Foo", "Bar"), Array<String>::class)
    }

    @Test
    fun testMap() {
        fun testEntry(map: Map<String, Any?>, key: String, value: Any?) {
            assertContains(map, key, "Expecting entry for '$key'")
            assertEquals(value, map[key], "Checking value for entry '$key'")
        }

        val map = mapOf(
            "key" to "value",
            "number" to 123.0,
            "boolean" to true,
            "decimal" to 234.123,
        )

        val parsed = Gson().fromJson<Map<String, Any?>>(map.asString(), Map::class.java)

        map.keys.forEach {
            testEntry(parsed, it, map[it])
        }
    }

}