package io.redlink.more.more_app_mutliplatform

import kotlin.test.Test
import kotlin.test.assertTrue

class IosGreetingTests {

    @Test
    fun testGreet() {
        assertTrue("iOS" in Greeting().greet())
    }


}