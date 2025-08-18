package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.ExerciseSessionData
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthkitType_exercise
import kotlinx.coroutines.launch



private const val TAG = "healthkit-mobile-observation:Exercise_observation"



private val permissions = setOf(
    HealthPermissions.READ_EXERCISE,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
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
                    storeData(ExerciseSessionData(record))
                }

                stop { println("Stopped after data collection") }


            } catch (e: Exception) {
                println("Error: ${e.message}")
                stop { println("Stopped after error") }
            }
        }

        return true
    }

    /*
    override suspend fun hasPermissions(): Boolean {
        println("fetching permissions")
        println(getPermission())
        val permissions = getPermission()
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        println("GRANTED PERMISSIONS: $grantedPermissions")
        val hasPermission = healthConnectManager.hasAllPermissions(permissions)
        println(hasPermission)
        println("!!!!!!!!!!!!!!!!!")
        if (!hasPermission) {
            Napier.d { "Missing HealthKit permissions for reading data" }
        }
        return hasPermission
    }*/

}