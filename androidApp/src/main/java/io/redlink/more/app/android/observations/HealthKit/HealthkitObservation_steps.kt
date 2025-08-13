package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.SleepSessionData
import    io.redlink.more.app.android.observations.HealthKit.HealthKitObservation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthkitType_steps
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

private const val TAG = "healthkit-mobile-observation:Steps_observation"



private val permissions = setOf(
    HealthPermissions.READ_STEPS,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)



class HealthkitObservation_steps(context: Context): BaseHealthKitObservation<StepsRecord>(context,
    HealthkitType_steps(

    )) {

    override val recordClass: Class<StepsRecord>
        get() = StepsRecord::class.java

    override fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        println("Steps reading from Healthkit")
        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    println("Missing Health Connect permissions")
                    stop { println("Stopped: No permissions") }
                    return@launch
                }
                val records = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start_time.toInstant(), now.toInstant())
                )
                for (record in records) {
                    println(record)
                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    storeData(record)
                }
                stop { println("records sent") }
            }
            catch (e:Exception){
                println("Error: ${e.message}")
                stop { println("Stopped after error") }
            }
        }
        return true

    }



}