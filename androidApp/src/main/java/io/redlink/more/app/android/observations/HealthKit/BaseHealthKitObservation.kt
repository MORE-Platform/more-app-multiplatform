package io.redlink.more.app.android.observations.HealthKit

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.Record
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.activities.healthPage.HealthConnectManager
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.ObservationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.time.ZonedDateTime

abstract class BaseHealthKitObservation<T : Record>(
    private val context: Context,
    repository: MainRepository,
    observationType: ObservationType
) : Observation(repository, observationType) {
    protected val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    protected val healthConnectManager = HealthConnectManager(context)
    protected val healthConnectClient = HealthConnectClient.getOrCreate(context)
    protected val now = ZonedDateTime.now()
    var start_time = now.minusDays(1)
    protected val scope = CoroutineScope(Job() + Dispatchers.IO)
    protected val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    // Each subclass defines the record type it handles
    abstract val recordClass: Class<T>
    protected var observationJob: Job? = null

    override fun start(): Boolean {
        TODO("Not yet implemented")
    }

    override fun stop(onCompletion: () -> Unit) {
        observationJob?.cancel()
        observationJob = null
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        Napier.e("observation config called")
        try {
            settings["daysback"]?.toString()?.trim('\"')?.toLong()?.let {
                println(it)
                start_time = now.minusDays(it)
            }
        } catch (e: java.lang.Exception) {
            Napier.e("Error when setting up observation config ${e.stackTraceToString()}")
        }

    }

    abstract fun getPermission(): Set<String>

    override fun ableToAutomaticallyStart(): Boolean {
        return true
    }

    protected suspend fun hasPermissions(): Boolean {
        val requiredPermissions = getPermission()
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        println(grantedPermissions)
        val missingPermissions = requiredPermissions - grantedPermissions

        return if (missingPermissions.isEmpty()) {
            Napier.d { "✅ All Health Connect permissions granted" }
            true
        } else {
            Napier.w { "⚠️ Missing permissions: $missingPermissions" }
            false
        }

    }
}