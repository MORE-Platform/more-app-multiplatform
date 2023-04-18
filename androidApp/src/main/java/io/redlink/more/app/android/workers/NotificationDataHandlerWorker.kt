package io.redlink.more.more_app_mutliplatform.android.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

/**
 * This worker handles data received by a push notification, since the app gets only a few seconds to handle those incoming data.
 */
private const val TAG = "NotificationDataHandlerWorker"
const val NOTIFICATION_DATA = "notification_data"

class NotificationDataHandlerWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Notification Worker started!")
            val data = inputData.getString(NOTIFICATION_DATA)
            val type: Type = object : TypeToken<Map<String, String>>() {}.type
            val notificationData: Map<String, String> = Gson().fromJson(data, type)
            //TODO: Handle incoming data
            Log.d(TAG, "Data: $data")
            Log.d(TAG, "NotificationData: $notificationData")
            Result.success()
        } catch (err: Exception) {
            Log.e(TAG, err.stackTraceToString())
            Result.failure()
        }
    }

}