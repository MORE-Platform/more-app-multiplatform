package io.redlink.more.app.android.observations.HealthKit.DataFormatter

import androidx.health.connect.client.records.SleepSessionRecord
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.json.JSONObject

class SleepSessionData(record: SleepSessionRecord) {

    val startTimestamp: String
    val endTimestamp: String
    val type : String
    init {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm")
            .withLocale(Locale.US)
            .withZone(ZoneOffset.UTC) // Use record.startZoneOffset if needed

        startTimestamp = formatter.format(record.startTime)
        endTimestamp = formatter.format(record.endTime)
        type = record.stages.toString()

    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("startTimestamp", startTimestamp)
        json.put("endTimestamp", endTimestamp)
        json.put("type", type)
        return json
    }
}
