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
import io.redlink.more.app.android.activities.healthPage.HealthConnectManager
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthKitType_HR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Error
import java.time.ZonedDateTime
import kotlin.reflect.KClass

private const val TAG = "healthkit-mobile-observation"

private val permissions = setOf(
    HealthPermissions.READ_STEPS,
    HealthPermissions.READ_HEART_RATE,
    HealthPermissions.READ_EXERCISE,
    HealthPermissions.READ_WEIGHT,
    HealthPermissions.READ_SLEEP,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)

private val permissionMapping : Map <String, KClass<out Record>> = mapOf(
    HealthPermissions.READ_STEPS to StepsRecord::class,
    HealthPermissions.READ_HEART_RATE to HeartRateRecord::class,
    HealthPermissions.READ_EXERCISE to ExerciseSessionRecord::class,
    HealthPermissions.READ_WEIGHT to WeightRecord::class,
    HealthPermissions.READ_SLEEP to SleepSessionRecord::class
)

class HealthKitObservation(context: Context ):Observation(observationType =HealthKitType_HR(healthPermissions = permissions) ) {


    private val healthConnectManager = HealthConnectManager(context)

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        println("dataREad")
        Napier.d { "Trying to read steps using health Kit"}
        if (this.hasPermission()){
            val listener = this
            scope.launch {
                 if(!healthConnectManager.hasAllPermissions(permissions)) throw Error(
                     "Not all Permissions granted"
                 )
                else{


                    //reading health data in last 1 day
                    val now = ZonedDateTime.now()
                    val start = now.minusDays( 1)
                    for(recordKClass in permissionMapping.values){
                        when (recordKClass) {
                            StepsRecord::class -> println(healthConnectManager.readData<StepsRecord>(TimeRangeFilter.between(start.toInstant(), now.toInstant())))
                            HeartRateRecord::class -> println(healthConnectManager.readData<HeartRateRecord>(TimeRangeFilter.between(start.toInstant(), now.toInstant())))
                            ExerciseSessionRecord::class -> println(healthConnectManager.readData<ExerciseSessionRecord>(TimeRangeFilter.between(start.toInstant(), now.toInstant())))
                            WeightRecord::class -> println(healthConnectManager.readData<WeightRecord>(TimeRangeFilter.between(start.toInstant(), now.toInstant())))
                            SleepSessionRecord::class -> println(healthConnectManager.readData<SleepSessionRecord>(TimeRangeFilter.between(start.toInstant(), now.toInstant())))
                            else -> println("Unsupported record: $recordKClass")
                        }                    }
                }
            }
            return true
        }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        //todo filtering what data do we want

    }

    private fun hasPermission():Boolean{
        return this.hasPermissions(MoreApplication.appContext!!)
    }
    override fun ableToAutomaticallyStart() = true

    private fun hasPermissions(context: Context): Boolean {
        getPermission().forEach{
            permission -> if(
                ActivityCompat.checkSelfPermission(context,permission)== PackageManager.PERMISSION_DENIED
            ){
                Napier.d{ " Has no HealthKit permissions for reading  data "}
                return false
            }
        }
        return true
    }
}