package io.redlink.more.app.android.datastreaming.workers

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.work.*
import androidx.health.connect.client.HealthConnectFeatures
import io.redlink.more.app.android.activities.healthPage.HealthConnectManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter

import kotlinx.coroutines.*
import java.time.ZonedDateTime

/*class RawDataUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val healthConnectManager: HealthConnectManager,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)

        val endTime = ZonedDateTime.now().withNano(0)
        val startTime = endTime.minusDays(1)

        val featureStatus = healthConnectClient.features.getFeatureStatus(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
        )

        if (featureStatus == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE) {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()

            if (!grantedPermissions.contains(HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)) {
                Log.d("RawDataUploadWorker", "Background permission not granted — reading in foreground.")
                // TODO: Perform read in foreground if appropriate (but we're in a Worker, so limited)
            }
            val end = ZonedDateTime.now().withNano(0)
            val start = end.minusDays(1)
            // Whether or not permission was granted, we try reading (only works if permission was granted)
            return try {
                val response = healthConnectManager.readData<StepsRecord>(
                    TimeRangeFilter.between(start.toInstant(), end.toInstant())
                )
                val totalSteps = response.records.sumOf { it.count }
                Log.d("RawDataUploadWorker", "Total steps in past 24h: $totalSteps")

                Result.success()
            } catch (e: Exception) {
                Log.e("RawDataUploadWorker", "Failed to read steps data", e)
                Result.failure()
            }
        } else {
            Log.d("RawDataUploadWorker", "Background feature not available.")
            return Result.failure()
        }
    }
}




*/
