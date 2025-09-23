package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.SleepSessionData
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealtkitType_Sleep
import kotlinx.coroutines.launch

private const val TAG = "healthkit-mobile-observation:Sleep_observation"

private val permissions = setOf(
    HealthPermissions.READ_SLEEP,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)

class HealthkitObservation_Sleep(
    context: Context,
    repository: MainRepository,
) : BaseHealthKitObservation<SleepSessionRecord>(
    context,
    repository,
    HealtkitType_Sleep()
) {

    override val recordClass: Class<SleepSessionRecord>
        get() = SleepSessionRecord::class.java

    override fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    Napier.e("Missing Health Connect permissions")
                    stop { Napier.e("Stopped: No permissions") }
                    return@launch
                }

                val records = healthConnectManager.readData<SleepSessionRecord>(
                    TimeRangeFilter.between(startTime.toInstant(), now.toInstant())
                )

                for (record in records) {
                    if (!sendingRawData){
                    storeData(SleepSessionData(record).toJson())}

                }
                if (sendingRawData){
                    storeData(mapOf("Sleep records" to records),-1)
                }
                stop { Napier.d("Stopped after data collection") }

            } catch (e: Exception) {
                Napier.e("Error: ${e.message}")
                stop { Napier.e("Stopped after error") }
            }
        }

        return true
    }

}