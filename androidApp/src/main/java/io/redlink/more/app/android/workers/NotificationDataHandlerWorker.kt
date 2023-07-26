package io.redlink.more.app.android.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.models.StudyState
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

    private val shared: Shared

    init {
        if (MoreApplication.shared == null) {
            MoreApplication.initShared(applicationContext)
        }
        shared = MoreApplication.shared!!
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Notification Worker started!")
            val data = inputData.getString(NOTIFICATION_DATA)
            val type: Type = object : TypeToken<Map<String, String>>() {}.type
            val notificationData: Map<String, String> = Gson().fromJson(data, type)
            Log.d(TAG, "Data: $data")
            Log.d(TAG, "NotificationData: $notificationData")
            if (notificationData.isNotEmpty()) {
                if (notificationData[MAIN_DATA_KEY] == STUDY_CHANGED) {
                    updateStudy(notificationData)
                }
            }
            Result.success()
        } catch (err: Exception) {
            Log.e(TAG, err.stackTraceToString())
            Result.failure()
        }
    }

    private suspend fun updateStudy(data: Map<String, String>) {
        val oldStudyState =
            data[STUDY_OLD_STATE]?.let { StudyState.getState(it) }
        val newStudyState =
            data[STUDY_NEW_STATE]?.let { StudyState.getState(it) }
        shared.updateStudy(oldStudyState, newStudyState)
    }

    companion object {
        private const val MAIN_DATA_KEY = "key"
        private const val STUDY_CHANGED = "STUDY_STATE_CHANGED"

        private const val STUDY_OLD_STATE = "oldState"
        private const val STUDY_NEW_STATE = "newState"
    }

}

