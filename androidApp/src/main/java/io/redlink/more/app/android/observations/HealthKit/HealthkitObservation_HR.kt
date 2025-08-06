package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import androidx.core.app.ActivityCompat
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthKitType_HR
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

private const val  TAG = "healthkit-mobile-observation:HR_observation"

private val permissions = setOf(
    HealthPermissions.READ_HEART_RATE,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)

private val permissionMapping : Map <String, KClass<out Record>> = mapOf(
    HealthPermissions.READ_HEART_RATE to HeartRateRecord::class
)



class HealthkitObservation_HR(context: Context) :
    BaseHealthKitObservation<HeartRateRecord>(
        context,
        HealthKitType_HR(
            healthPermissions = permissions
        )
    ){

    override val recordClass: Class<HeartRateRecord>
        get() = HeartRateRecord::class.java

    override  fun getPermission(): Set<String> = permissions


    override fun start(): Boolean {
        if (this.hasPermission()){
            val listener = this
            scope.launch {
                if(!healthConnectManager.hasAllPermissions(permissions)) throw Error(
                    "Permissions not granted for ${recordClass}"
                )
                else{
                    for (recordKClass in permissionMapping.values) {
                        if (recordKClass.java == recordClass) {
                            val records = healthConnectManager.readData<HeartRateRecord>(
                                TimeRangeFilter.between(start_time.toInstant(),now.toInstant()))
                            for(record in records){
                                println(record)
                                storeData(record)
                            }

                        }
                    }
                }
            }
            return true
        }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        super.stop(onCompletion)
    }






}