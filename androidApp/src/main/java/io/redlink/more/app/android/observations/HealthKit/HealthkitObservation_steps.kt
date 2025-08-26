package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthkitType_steps
import kotlinx.coroutines.launch
import androidx.health.connect.client.permission.HealthPermission
import com.google.gson.JsonPrimitive
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.StepSessionData
import okhttp3.internal.format
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.ZoneOffset



// Step permissions for Health Connect
val PERMISSIONS =
    setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )
private const val TAG = "healthkit-mobile-observation:Steps_observation"

// Define permissions as HealthPermissions.Permission

class HealthkitObservation_steps(context: Context) :
    BaseHealthKitObservation<StepsRecord>(
        context,
        HealthkitType_steps()
    ) {

    override val recordClass: Class<StepsRecord>
        get() = StepsRecord::class.java

    // Return HealthPermissions.Permission instead of String
    override fun getPermission(): Set<String> = PERMISSIONS

    override fun start(): Boolean {
        println("Steps reading from Healthkit")
        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    println("Missing Health Connect permissions")
                    stop { println("Stopped: No permissions") }
                    return@launch
                }
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss")
                    .withLocale(Locale.GERMANY)
                    .withZone(ZoneOffset.UTC)
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start_time.toInstant(), now.toInstant())
                )
                var stepcount = 0L
                for (record in records) {
                    println(record)
                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    stepcount +=  record.count

                    println("RECORD OF THIS SENT TO THE BACKEND")
                }
                storeData(mapOf("steps" to stepcount, "start" to formatter.format(start_time), "end" to formatter.format(now)))
                stop { println("records sent") }
            } catch (e: Exception) {
                println("Error: ${e.message}")
                stop { println("Stopped after error") }
            }
        }
        return true
    }




}
