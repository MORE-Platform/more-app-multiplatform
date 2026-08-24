package io.redlink.more.formatter

import dev.icerock.moko.resources.desc.desc
import io.redlink.more.observations.healthConnect.HealthConnectDataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HealthConnectValueFormatterTest {

    @Test
    fun `format returns null for non health connect observation types`() {
        assertNull(HealthConnectValueFormatter.format("some-other-type", "{\"hr\":72}"))
    }

    @Test
    fun `format returns null when current value is not a string`() {
        assertNull(HealthConnectValueFormatter.format(HealthConnectDataType.HEART_RATE.subTypeValue, 72))
    }

    @Test
    fun `format extracts heart rate value from stored json`() {
        val value = HealthConnectValueFormatter.format(
            HealthConnectDataType.HEART_RATE.subTypeValue,
            "{\"timestamp\":\"2026-01-01T00:00:00Z\",\"data\":{\"hr\":72}}"
        )

        assertEquals("72", value?.value)
    }

    @Test
    fun `format extracts step count value from stored json`() {
        val value = HealthConnectValueFormatter.format(
            HealthConnectDataType.STEPS.subTypeValue,
            "{\"timestamp\":\"2026-01-01T00:00:00Z\",\"startTime\":\"2026-01-01T00:00:00Z\"," +
                "\"endTime\":\"2026-01-01T00:00:00Z\",\"data\":{\"steps\":1000}}"
        )

        assertEquals("1000", value?.value)
    }

    @Test
    fun `format shows steps together with the goal when present`() {
        val value = HealthConnectValueFormatter.format(
            HealthConnectDataType.STEPS.subTypeValue,
            "{\"timestamp\":\"2026-01-01T00:00:00Z\",\"data\":{\"steps\":1000,\"stepsGoal\":10000}}"
        )

        assertEquals("1000 / 10000", value?.value)
    }

    @Test
    fun `format resolves label and unit from the data type metadata`() {
        val value = HealthConnectValueFormatter.format(
            HealthConnectDataType.HEART_RATE.subTypeValue,
            "{\"timestamp\":\"2026-01-01T00:00:00Z\",\"data\":{\"hr\":72}}"
        )

        assertEquals(HealthConnectDataType.HEART_RATE.unit.desc(), value?.unit)
        assertEquals(HealthConnectDataType.HEART_RATE.label.desc(), value?.label)
    }

    @Test
    fun `format falls back to a dash when the value key is missing from the payload`() {
        val value = HealthConnectValueFormatter.format(
            HealthConnectDataType.STEPS.subTypeValue,
            "{\"timestamp\":\"2026-01-01T00:00:00Z\",\"data\":{}}"
        )

        assertEquals("-", value?.value)
    }

    @Test
    fun `format returns null when the payload has no data object`() {
        val value = HealthConnectValueFormatter.format(
            HealthConnectDataType.HEART_RATE.subTypeValue,
            "{\"timestamp\":\"2026-01-01T00:00:00Z\"}"
        )

        assertNull(value)
    }
}
