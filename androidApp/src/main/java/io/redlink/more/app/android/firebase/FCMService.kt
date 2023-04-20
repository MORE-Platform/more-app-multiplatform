package io.redlink.more.app.android.firebase

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.services.PushNotificationService
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import com.google.gson.Gson
import io.redlink.more.app.android.workers.NOTIFICATION_DATA
import io.redlink.more.app.android.workers.NotificationDataHandlerWorker
import kotlinx.coroutines.launch

private const val TAG = "FCMService"

/*
Service to handle push notifications and firebase connections
 */

class FCMService : FirebaseMessagingService() {

    lateinit var workManager : WorkManager

    private val notificationRepository = NotificationRepository()

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        val oldToken = sharedPreferencesRepository.load(FCM_TOKEN, "")
        if (token.isNotEmpty() && token.isNotBlank() && oldToken != token) {
            sharedPreferencesRepository.store(FCM_TOKEN, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "From: ${message.from}")
        if (message.data.isNotEmpty() || message.notification != null) {
            scope.launch {
                notificationRepository.storeNotification(PushNotificationService.daoFromRemoteMessage(message))
            }
            if (message.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload: ${message.data}")
                try {
                    val inputData = workDataOf(NOTIFICATION_DATA to Gson().toJson(message.data))
                    val workRequest = OneTimeWorkRequestBuilder<NotificationDataHandlerWorker>()
                        .setInputData(inputData)
                        .build()
                    workManager.enqueue(workRequest)
                } catch (e: Exception) {
                    Log.e(TAG, e.stackTraceToString())
                    Firebase.crashlytics.recordException(e)
                }
            }

            message.notification?.let {
                Log.d(TAG, "Message Title: ${it.title}")
                Log.d(TAG, "Message Notification Body: ${it.body}")
                val title = it.title ?: return
                val body = it.body ?: return

                PushNotificationService.sendNotification(this, title, body)
            }
        }
    }

    companion object {
        private const val FCM_TOKEN = "FCM_TOKEN"
        private val scope = CoroutineScope(Job() + Dispatchers.Default)

        private val sharedPreferencesRepository: SharedStorageRepository = SharedPreferencesRepository(context = MoreApplication.appContext!!)
        private val networkService = NetworkService(
            EndpointRepository(sharedPreferencesRepository),
            CredentialRepository(sharedPreferencesRepository)
        )
        fun newFirebaseToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                } else {
                    Log.w(TAG, "FCM_Token: ${task.result}")
                    scope.launch {
                        networkService.sendNotificationToken(task.result)
                    }
                }
            })
        }

        fun deleteFirebaseToken(completionHandler: OnCompleteListener<Void>) {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(completionHandler)
        }
    }
}