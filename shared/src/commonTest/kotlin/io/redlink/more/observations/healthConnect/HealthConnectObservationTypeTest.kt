package io.redlink.more.observations.healthConnect

import io.redlink.more.HEALTH_CONNECT_PREFIX
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HealthConnectObservationTypeTest {

    private val heartRateType = "$HEALTH_CONNECT_PREFIX-heart-rate-observation"
    private val stepsType = "$HEALTH_CONNECT_PREFIX-steps-observation"

    @Test
    fun testMatchesBothSubTypesAndBaseType() {
        val observationType = HealthConnectObservationType()
        assertTrue(observationType.matches(heartRateType))
        assertTrue(observationType.matches(stepsType))
        assertTrue(observationType.matches("$HEALTH_CONNECT_PREFIX-observation"))
        assertFalse(observationType.matches("other-observation"))
    }
}
