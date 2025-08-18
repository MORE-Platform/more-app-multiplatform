package io.redlink.more.app.android.observations.HealthKit.DataFormatter
import androidx.health.connect.client.records.HeartRateRecord
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


class HrSessionData(record: HeartRateRecord) {

    val startTimestamp: String
    val endTimestamp: String
    val startZoneOffsetSeconds: Int?
    val endZoneOffsetSeconds: Int?
    val samples: List<Map<String, Any>>
    val metadataId: String?

    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss")
            .withLocale(Locale.US)
            .withZone(ZoneOffset.UTC)

        startTimestamp = formatter.format(record.startTime)
        endTimestamp = formatter.format(record.endTime)

        startZoneOffsetSeconds = record.startZoneOffset?.totalSeconds
        endZoneOffsetSeconds = record.endZoneOffset?.totalSeconds
        metadataId = record.metadata.id

        // Flatten to JSON-friendly primitives
        samples = record.samples.map { sample ->
            mapOf(
                "time" to formatter.format(sample.time),
                "bpm" to sample.beatsPerMinute
            )
        }
    }

    fun toJson(): String {
        return """
        {
          "startTimestamp": "$startTimestamp",
          "endTimestamp": "$endTimestamp",
          "startZoneOffsetSeconds": $startZoneOffsetSeconds,
          "endZoneOffsetSeconds": $endZoneOffsetSeconds,
          "metadataId": "$metadataId",
          "samples": ${samples.joinToString(
            prefix = "[",
            postfix = "]"
        ) { """{"time":"${it["time"]}","bpm":${it["bpm"]}}""" }}
        }
        """.trimIndent()
    }
}