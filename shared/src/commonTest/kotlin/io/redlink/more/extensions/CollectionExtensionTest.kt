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
