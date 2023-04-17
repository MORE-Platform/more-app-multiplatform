package io.redlink.more.more_app_mutliplatform

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTests {
    @Test
    fun testGreet() {
        assertTrue(Greeting().greet().isNotEmpty())
    }
}