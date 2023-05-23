package io.redlink.more.app.android.services

import android.app.*
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ObservationRecordingService: Service() {
    private var observationManager: ObservationManager? = null
    private val scheduleRepository = ScheduleRepository()
    private var observationFactory: ObservationFactory? = null
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (observationFactory == null) {
            observationFactory = MoreApplication.observationFactory ?: AndroidObservationFactory(this)
        }
        observationFactory?.let {
            if (observationManager == null) {
                observationManager = MoreApplication.observationManager ?: ObservationManager(it)
            }
        }
        return intent?.action?.let { action ->
            return@let when (action) {
                SERVICE_RECEIVER_START_ACTION -> {
                    intent.getStringExtra(SCHEDULE_ID)?.let {
                        startObservation(it)
                        return super.onStartCommand(intent, flags, startId)
                    }
                    START_NOT_STICKY
                }
                SERVICE_RECEIVER_PAUSE_ACTION -> {
                    intent.getStringExtra(SCHEDULE_ID)?.let {
                        pauseObservation(it)
                    }
                    START_NOT_STICKY
                }
                SERVICE_RECEIVER_STOP_ACTION -> {
                    intent.getStringExtra(SCHEDULE_ID)?.let {
                        stopObservation(it)
                    }
                    START_NOT_STICKY
                }
                SERVICE_RECEIVER_STOP_ALL_ACTION -> {
                    stopAll()
                    START_NOT_STICKY
                }
                SERVICE_RECEIVER_RESTART_ALL_STATES -> {
                    restartAll()
                    return super.onStartCommand(intent, flags, startId)
                }
                SERVICE_RECEIVER_UPDATE_STATES -> {
                    updateTaskStates()
                    START_NOT_STICKY
                }
                else -> {
                    START_NOT_STICKY
                }
            }
        } ?: START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
    }

    private fun startObservation(scheduleId: String) {
        Napier.d { "Starting the foreground service for scheduleId: $scheduleId..." }
        startForegroundService()
        scope.launch {
            if (observationManager?.start(scheduleId) == false) {
                stopObservation(scheduleId)
            }
        }
    }

    private fun pauseObservation(scheduleId: String) {
        observationManager?.pause(scheduleId)
    }

    private fun stopObservation(scheduleId: String) {
        observationManager?.stop(scheduleId)
        scheduleRepository.setCompletionStateFor(scheduleId, true)
        if (observationManager?.hasRunningTasks() == false) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            running = false
            stopSelf()
        }
    }

    private fun stopAll() {
        observationManager?.stopAll()
        if (observationManager?.hasRunningTasks() == false) {
            running = false
            stopSelf()
        }
    }

    private fun restartAll() {
        Napier.d { "Starting the foreground service..." }
        startForegroundService()
        scope.launch {
            val startedObservations = observationManager?.restartStillRunning() ?: emptySet()
            if (startedObservations.isEmpty()) {
                if (observationManager?.hasRunningTasks() == false) {
                    stopAll()
                }
            }
        }
    }

    private fun updateTaskStates() {
        observationManager?.updateTaskStates()
    }

    private fun startForegroundService() {
        startForeground(
            1001,
            buildNotification(
                channelId = getString(R.string.default_channel_id),
                notificationTitle = getString(R.string.more_observation_running),
                notificationText = getString(R.string.more_observation_notification_explanation)
            )
        )
        running = true
    }

    private fun buildNotification(
        channelId: String,
        notificationTitle: String,
        notificationText: String,
    ): Notification {
        val channel = NotificationChannel(
            channelId,
            channelId,
            NotificationManager.IMPORTANCE_LOW
        )

        val intent = Intent(this, ContentActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
        return Notification.Builder(applicationContext, channelId)
            .setContentText(notificationText)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.mipmap.ic_more_logo_hf_v2)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        const val SCHEDULE_ID = "SCHEDULE_ID"
        const val SERVICE_RECEIVER_START_ACTION = "io.redlink.more.app.android.START_SERVICE"
        const val SERVICE_RECEIVER_PAUSE_ACTION = "io.redlink.more.app.android.PAUSE_SERVICE"
        const val SERVICE_RECEIVER_STOP_ACTION = "io.redlink.more.app.android.STOP_SERVICE"
        const val SERVICE_RECEIVER_STOP_ALL_ACTION = "io.redlink.more.app.android.STOP_ALL_SERVICE"
        const val SERVICE_RECEIVER_UPDATE_STATES = "io.redlink.more.app.android.UPDATE_STATES"
        const val SERVICE_RECEIVER_RESTART_ALL_STATES = "io.redlink.more.app.android.RESTART_ALL"

        var running = false
            private set

        fun start(
            scheduleId: String,
        ) {
            val serviceIntent = Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_START_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            try {
                Handler(Looper.getMainLooper()).post {
                    MoreApplication.appContext?.startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                Napier.e(e.stackTraceToString())
            }
        }

        fun pause(scheduleId: String) {
            val serviceIntent = Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_PAUSE_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            MoreApplication.appContext?.startService(serviceIntent)
        }

        fun stop(scheduleId: String) {
            val serviceIntent = Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_STOP_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            MoreApplication.appContext?.startService(serviceIntent)
        }

        fun stopAll() {
            val serviceIntent = Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_STOP_ALL_ACTION
            MoreApplication.appContext?.startService(serviceIntent)
        }

        fun restartAll() {
            val serviceIntent = Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES
            try {
                Handler(Looper.getMainLooper()).post {
                    MoreApplication.appContext?.startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                Napier.e(e.stackTraceToString())
            }
        }

        fun updateTaskStates() {
            val serviceIntent = Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_UPDATE_STATES
            MoreApplication.appContext?.startService(serviceIntent)
        }
    }
}