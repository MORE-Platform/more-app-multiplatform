package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthkitType_steps
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

// Step permissions for Health Connect
val PERMISSIONS =
    setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )
private const val TAG = "healthkit-mobile-observation:Steps_observation"

// Define permissions as HealthPermissions.Permission

class HealthkitObservation_steps(
    context: Context,
    repository: MainRepository,
) :
    BaseHealthKitObservation<StepsRecord>(
        context,
        repository,
        HealthkitType_steps()
    ) {

    override val recordClass: Class<StepsRecord>
        get() = StepsRecord::class.java

    // Return HealthPermissions.Permission instead of String
    override fun getPermission(): Set<String> = PERMISSIONS

    override fun start(): Boolean {

        val rawDataList: MutableList<StepsRecord> = mutableListOf()
        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    Napier.e("Missing Health Connect permissions")
                    stop { Napier.e("Stopped: No permissions") }
                    return@launch
                }
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss")
                    .withLocale(Locale.GERMANY)
                    .withZone(ZoneOffset.UTC)
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(startTime.toInstant(), now.toInstant())
                )
                if(!sendingRawData){
                var stepcount = 0L
                for (record in records) {
                    stepcount += record.count
                }
                storeData(
                    mapOf(
                        "steps" to stepcount,
                        "start" to formatter.format(startTime),
                        "end" to formatter.format(now)
                    )
                )}
                else{
                    storeData(mapOf("raw step records" to records),-1)
                }
                //stop { Napier.d("records sent") }
                true
            } catch (e: Exception) {
               Napier.e("Error: ${e.message}")
                //stop { Napier.e("Stopped after error") }
                false
            }
        }
        return false
    }

}
