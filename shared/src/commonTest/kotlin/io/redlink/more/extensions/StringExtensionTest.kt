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
package io.redlink.more.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringExtensionTest {

    @Test
    fun testExtractRouteFromDeepLink() {
        assertEquals("notifications", "app://host/notifications".extractRouteFromDeepLink())
        assertEquals("notifications", "app://host/notifications/123".extractRouteFromDeepLink())
        assertEquals(
            "notifications",
            "app://host/notifications/123?param=value".extractRouteFromDeepLink()
        )
        assertEquals("profile", "app://host/profile".extractRouteFromDeepLink())
    }

    @Test
    fun testDecodeURIComponent() {
        assertEquals("hello world", "hello+world".decodeURIComponent())
        assertEquals("hello world", "hello%20world".decodeURIComponent())
    }

    @Test
    fun testMapQueryParams() {
        val query = "?param1=value1&param2=value2&param1=value3"
        val params = query.mapQueryParams()
        assertEquals(2, params.size)
        assertEquals(setOf("value1", "value3"), params["param1"])
        assertEquals(setOf("value2"), params["param2"])
    }

    @Test
    fun testOverlaps() {
        assertTrue("hello world".overlaps("hello"))
        assertTrue("hello".overlaps("hello world"))
        assertFalse("hello".overlaps("world"))
        assertTrue("HELLO".overlaps("hello", ignoreCase = true))
    }
}
