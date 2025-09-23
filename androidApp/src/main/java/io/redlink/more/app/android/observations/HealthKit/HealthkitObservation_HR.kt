package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.HrSessionData
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthKitType_HR
import kotlinx.coroutines.launch

private const val TAG = "healthkit-mobile-observation:HR_observation"

private val permissions = setOf(
    HealthPermissions.READ_HEART_RATE,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)

class HealthkitObservation_HR(
    context: Context,
    repo: MainRepository,
) :
    BaseHealthKitObservation<HeartRateRecord>(
        context,
        repo as MainRepository,
        HealthKitType_HR()
    ) {

    override val recordClass: Class<HeartRateRecord>
        get() = HeartRateRecord::class.java

    override fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        Napier.d("HR reading from Healthkit")
        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    Napier.e("Missing Health Connect permissions")
                    stop { Napier.e("Stopped: No permissions") }
                    return@launch
                }
                val records = healthConnectManager.readData<HeartRateRecord>(
                    TimeRangeFilter.between(startTime.toInstant(), now.toInstant())
                )

                for (record in records) {
                    if(!sendingRawData){storeData(HrSessionData(record))}


                }
                if (sendingRawData){
                    Napier.d { "Sending this raw data to backend ${records}" }
                    storeData(mapOf("records" to records),-1)
                }
                stop { Napier.d("records sent") }
            } catch (e: Exception) {
                Napier.e("Error: ${e.message}")
                stop { Napier.d("Stopped after error") }
            }
        }
        return true

    }

}