package io.redlink.more.app.android.datastreaming

import android.content.Context
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.redlink.more.app.android.activities.healthPage.HealthConnectManager
import java.time.ZonedDateTime
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InterventionStreamer(context:Context,workerParameters: WorkerParameters ) :CoroutineWorker(context,workerParameters){
    //Todo, add option for this plugin in the application, then we can check start of the init that we are
    override suspend fun doWork(): Result {
        return try {
            val uploadSuccess = uploadDataToServer()

            if (uploadSuccess) {
                Result.success()
            } else {
                Result.retry()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
    val healthConnectManager: HealthConnectManager = HealthConnectManager(context)

    //generic
    inline suspend fun <reified T : Record> read_withHealthkit(end_time: ZonedDateTime,start_offset_days:Long, ): List<T> {
        var start = end_time.minusDays(start_offset_days)
        return healthConnectManager.readData<T>(TimeRangeFilter.between(start.toInstant(),end_time.toInstant()))
    }

    suspend fun read_steps(end_time: ZonedDateTime,start_offset_days: Long):List<StepsRecord>{
        return read_withHealthkit<StepsRecord>(end_time,start_offset_days)
    }

    suspend fun read_hr(end_time: ZonedDateTime,start_offset_days: Long):List<HeartRateRecord>{
        return read_withHealthkit<HeartRateRecord>(end_time,start_offset_days)
    }

    private suspend fun uploadDataToServer(): Boolean = withContext(Dispatchers.IO){

        val client = OkHttpClient()
        val steps = read_steps(end_time = ZonedDateTime.now().withNano(0), start_offset_days = 2)
        val stepsJson = steps.joinToString(prefix = "[", postfix = "]") { step ->
            """
    {
        "count": ${step.count},
        "startTime": "${step.startTime}",
        "endTime": "${step.endTime}"
    }
    """.trimIndent()
        }
        val json = """
            {
                "timestamp": "${System.currentTimeMillis()}",
                "message": "Sample data upload",
                "data" : $stepsJson
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://10.0.2.2:8511/send_notification")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        response.use {
            return@withContext it.isSuccessful
        }
    }


}