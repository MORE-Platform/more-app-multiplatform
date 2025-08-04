package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.health.connect.HealthPermissions
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthkitType_exercise

import kotlin.reflect.KClass


private const val TAG = "healthkit-mobile-observation:Exercise_observation"



private val permissions = setOf(
    HealthPermissions.READ_EXERCISE,
    HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND
)

private val permissionMapping : Map <String, KClass<out Record>> = mapOf(
    HealthPermissions.READ_EXERCISE to ExerciseSessionRecord::class,
)


class HealthkitObservation_exercise(context: Context): BaseHealthKitObservation<ExerciseSessionRecord>(context,
    HealthkitType_exercise(
        healthPermissions= permissions
    )) {

    override val recordClass: Class<ExerciseSessionRecord>
        get() = ExerciseSessionRecord::class.java


    override fun getPermission(): Set<String> = permissions

    override fun start(): Boolean {
        if (this.hasPermission()){
            //TODO implement start
            return true
        }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        super.stop(onCompletion)
    }
}