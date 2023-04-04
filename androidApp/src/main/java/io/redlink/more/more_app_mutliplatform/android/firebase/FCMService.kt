package io.redlink.more.more_app_mutliplatform.android.firebase

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.redlink.more.more_app_mutliplatform.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedPreferencesRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "FCMService"

/*
Service to handle push notifications and firebase connections
 */

class FCMService : FirebaseMessagingService() {
    /**
     * Method to handle incoming push notifications. Creates user facing push notification for notification data and worker for incoming background data.
     * IMPORTANT: Only work with the predefined Worker 'NotificationDataHandlerWorker' to handle incoming data.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "From: ${message.from}")
        Log.d(TAG, "$message")
        if (message.data.isNotEmpty() || message.notification != null) {
            if (message.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload: ${message.data}")
                try {

                } catch (e: Exception) {
                    Log.e(TAG, e.stackTraceToString())
                }
            }

            message.notification?.let {
                Log.d(TAG, "Message Title: ${it.title}")
                Log.d(TAG, "Message Notification Body: ${it.body}")
                val title = it.title ?: return
                val body = it.body ?: return
            }
        }

    }

    /**
     * When a new token is created, this method gets called by Firebase. This handles the token storage and upload to the MMB.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        val oldToken = sharedPreferencesRepository.load(FCM_TOKEN, "")
        if (token.isNotEmpty() && token.isNotBlank() && oldToken != token) {
            sharedPreferencesRepository.store(FCM_TOKEN, token)
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