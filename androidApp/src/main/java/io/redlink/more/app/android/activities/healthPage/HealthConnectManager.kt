package io.redlink.more.app.android.activities.healthPage
import android.content.Context
import android.os.Build
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record

import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import java.time.Duration

import androidx.health.connect.client.records.SleepSessionRecord


import java.time.ZonedDateTime
import java.time.ZoneOffset




// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    fun isFeatureAvailable(feature: Int): Boolean{
        return healthConnectClient
            .features
            .getFeatureStatus(feature) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }

    /**
     * Determines whether all the specified permissions are already granted. It is recommended to
     * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
     * permissions are already granted then there is no need to request permissions via
     * [PermissionController.createRequestPermissionResultContract].
     */
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {

        return PermissionController.createRequestPermissionResultContract()
    }



    /**
     * TODO: Returns the weekly average of [WeightRecord]s.
     */
    suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
        val request = AggregateRequest(
            metrics = setOf(WeightRecord.WEIGHT_AVG),
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.aggregate(request)
        return response[WeightRecord.WEIGHT_AVG]
    }

    /**
     * TODO: Obtains a list of [ExerciseSessionRecord]s in a specified time frame. An Exercise Session Record is a
     * period of time given to an activity, that would make sense to a user, e.g. "Afternoon run"
     * etc. It does not necessarily mean, however, that the user was *running* for that entire time,
     * more that conceptually, this was the activity being undertaken.
     */
    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }





    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
            ChangesTokenRequest(
                setOf(
                    ExerciseSessionRecord::class,
                    StepsRecord::class,
                    TotalCaloriesBurnedRecord::class,
                    HeartRateRecord::class,
                    WeightRecord::class
                )
            )
        )
    }

    /**
     * Retrieve changes from a changes token.
     */
    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {
                // As described here: https://developer.android.com/guide/health-and-fitness/health-connect/data-and-data-types/differential-changes-api
                // tokens are only valid for 30 days. It is important to check whether the token has
                // expired. As well as ensuring there is a fallback to using the token (for example
                // importing data since a certain date), more importantly, the app should ensure
                // that the changes API is used sufficiently regularly that tokens do not expire.
                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }


    /**
     * Convenience function to reuse code for reading data.
     */
    suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    suspend fun writeSteps(){
        val endTime = Instant.now()
        val startTime = endTime.minus(Duration.ofMinutes(5))

            val stepsRecord = StepsRecord(
                count = 120,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                metadata = Metadata.autoRecorded(
                    device = Device(type = Device.TYPE_WATCH)
                )
            )
            healthConnectClient.insertRecords(listOf(stepsRecord))
    }

    suspend fun insertSleepData() {
        val sleepStart = ZonedDateTime.now().minusHours(8)
        val sleepEnd = ZonedDateTime.now()

        val sleepSession = SleepSessionRecord(
            startTime = sleepStart.toInstant(),
            startZoneOffset = ZoneOffset.UTC,
            endTime = sleepEnd.toInstant(),
            endZoneOffset = ZoneOffset.UTC,
            title = "Test Sleep Session",
            notes = "Added via emulator",
            metadata = Metadata.autoRecorded(
                device = Device(type = Device.TYPE_WATCH)
            )
        )

        healthConnectClient.insertRecords(listOf(sleepSession))
    }


    suspend fun insertHeartRate(){
        val now = Instant.now()
        val startTime = now.minusSeconds(60) // 1 minute ago
        val endTime = now

        // Create dummy samples, e.g., heart rate every 15 seconds
        val samples = listOf(
            HeartRateRecord.Sample(time = startTime.plusSeconds(0), beatsPerMinute = 72),
            HeartRateRecord.Sample(time = startTime.plusSeconds(15), beatsPerMinute = 75),
            HeartRateRecord.Sample(time = startTime.plusSeconds(30), beatsPerMinute = 73),
            HeartRateRecord.Sample(time = startTime.plusSeconds(45), beatsPerMinute = 74),
            HeartRateRecord.Sample(time = endTime, beatsPerMinute = 76),
        )

        // Metadata - use default or create new metadata
        val metadata = Metadata.autoRecorded(
            device = Device(type = Device.TYPE_WATCH)
        )
        val heartRateRecord = HeartRateRecord(
            startTime = startTime,
            startZoneOffset = ZoneOffset.UTC,
            endTime = endTime,
            endZoneOffset = ZoneOffset.UTC,
            samples = samples,
            metadata = metadata
        )

        healthConnectClient.insertRecords(listOf(heartRateRecord))
    }

    // Insert Exercise Session data
    suspend fun insertExerciseSession(){
        val now = Instant.now()
        val startTime = now.minusSeconds(30 * 60) // 30 minutes ago
        val endTime = now

        val exerciseSessionRecord = ExerciseSessionRecord(
            startTime = startTime,
            startZoneOffset = ZoneOffset.UTC,
            endTime = endTime,
            endZoneOffset = ZoneOffset.UTC,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING, // example type
            title = "Morning Run",
            metadata = Metadata.autoRecorded(
                device = Device(type = Device.TYPE_WATCH)
            )
        )

        healthConnectClient.insertRecords(listOf(exerciseSessionRecord))
    }


    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    // Represents the two types of messages that can be sent in a Changes flow.
    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}

enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}