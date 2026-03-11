package io.redlink.more.extensions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionExtensionTest {

    @Test
    fun testIsSubsetOf() {
        val set1 = setOf(1, 2, 3)
        val set2 = setOf(1, 2, 3, 4, 5)
        val set3 = setOf(1, 2, 6)

        assertTrue(set1.isSubsetOf(set2))
        assertFalse(set3.isSubsetOf(set2))
        assertTrue(emptySet<Int>().isSubsetOf(set2))
    }
}
