package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.observations.HealthKit.DataFormatter.SleepSessionData
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealtkitType_Sleep
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


private const val  TAG = "healthkit-mobile-observation:Sleep_observation"


private val permissions = setOf(
    HealthPermissions.READ_SLEEP,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)

class HealthkitObservation_Sleep(context: Context):BaseHealthKitObservation<SleepSessionRecord>(
    context,
    HealtkitType_Sleep(

    )) {


    override val recordClass: Class<SleepSessionRecord>
        get() = SleepSessionRecord::class.java

    override  fun getPermission(): Set<String> = permissions


    override fun start(): Boolean {
        println("sleep observation called")

        observationJob = scope.launch {
            try {
                if (!hasPermissions()) {
                    println("Missing Health Connect permissions")
                    stop { println("Stopped: No permissions") }
                    return@launch
                }

                val records = healthConnectManager.readData<SleepSessionRecord>(
                    TimeRangeFilter.between(start_time.toInstant(), now.toInstant())
                )

                for (record in records) {
                    println(record)
                    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    storeData(SleepSessionData(record))
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