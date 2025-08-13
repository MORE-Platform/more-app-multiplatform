package io.redlink.more.app.android.observations.HealthKit.DataFormatter

import androidx.health.connect.client.records.SleepSessionRecord
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


class SleepSessionData(record: SleepSessionRecord) {

    val startTimestamp: String
    val endTimestamp: String
    val startZoneOffsetSeconds: Int?
    val endZoneOffsetSeconds: Int?
    val title: String?
    val notes: String?
    val metadataId: String?

    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm")
            .withLocale(Locale.US)
            .withZone(ZoneOffset.UTC) // Use record.startZoneOffset if needed

        startTimestamp = formatter.format(record.startTime)
        endTimestamp = formatter.format(record.endTime)

        startZoneOffsetSeconds = record.startZoneOffset?.totalSeconds
        endZoneOffsetSeconds = record.endZoneOffset?.totalSeconds
        title = record.title
        notes = record.notes
        metadataId = record.metadata.id
    }
}
