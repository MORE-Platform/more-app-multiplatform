package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.Record
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.healthPage.HealthConnectManager
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.HealthKitType_HR
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType


abstract  class BaseHealthKitObservation <T : Record> (
    private val context: Context,
    observationType: ObservationType
): Observation(observationType) {
    protected val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }
    protected  val healthConnectManager = HealthConnectManager(context)

    // Each subclass defines the record type it handles
    abstract val recordClass: Class<T>


    override fun start(): Boolean {
        TODO("Not yet implemented")
    }


    override fun stop(onCompletion: () -> Unit) {
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        //todo filtering what data do we want

    }


    abstract fun getPermission(): Set<String>

    fun hasPermission(): Boolean {
        return this.hasPermissions(MoreApplication.appContext!!)
    }

    fun hasPermissions(context: Context): Boolean {
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