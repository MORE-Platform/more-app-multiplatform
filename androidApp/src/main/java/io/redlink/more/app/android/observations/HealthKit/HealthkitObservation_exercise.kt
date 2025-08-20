package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import android.text.BoringLayout.Metrics
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.ExerciseSessionData
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthkitType_exercise
import kotlinx.coroutines.launch
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord

private const val TAG = "healthkit-mobile-observation:Exercise_observation"



private val permissions = setOf(
    HealthPermissions.READ_EXERCISE,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND,
    HealthPermissions.READ_DISTANCE,
    HealthPermissions.READ_ACTIVE_CALORIES_BURNED
)




class HealthkitObservation_exercise(context: Context): BaseHealthKitObservation<ExerciseSessionRecord>(context,
    HealthkitType_exercise(

    )) {



    override val recordClass: Class<ExerciseSessionRecord>
        get() = ExerciseSessionRecord::class.java


    override fun getPermission(): Set<String> = permissions


    override fun start(): Boolean {
        println("Exercise observation called")

        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    println("Missing Health Connect permissions")
                    stop { println("Stopped: No permissions") }
                    return@launch
                }

                val records = healthConnectManager.readData<ExerciseSessionRecord>(
                    TimeRangeFilter.between(start_time.toInstant(), now.toInstant())
                )

                for (record in records) {
                    println(record)
                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    val res = healthConnectClient.aggregate(
                        AggregateRequest(
                            metrics = setOf(DistanceRecord.DISTANCE_TOTAL,ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(record.startTime,record.endTime)
                        )
                    )
                    val distance = res[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
                    val calories = res[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inCalories ?: 0.0
                    storeData(ExerciseSessionData(record,distance,calories))

                }

                stop { println("Stopped after data collection") }


            } catch (e: Exception) {
                println("Error: ${e.message}")
                stop { println("Stopped after error") }
            }
        }

        return true
    }


}