/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ObservationRecordingService : Service() {
    private var observationManager: ObservationManager? = null
    private val scheduleRepository = ScheduleRepository()
    private var observationFactory: ObservationFactory? = null
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Napier.i { "ObservationRecordingService called..." }
        if (observationFactory == null) {
            if (MoreApplication.shared == null) {
                MoreApplication.initShared(applicationContext)
            }
            observationFactory = MoreApplication.shared!!.observationFactory
        }
        observationFactory?.let {
            if (observationManager == null) {
                observationManager =
                    MoreApplication.shared?.observationManager ?: ObservationManager(
                        it,
                        AndroidDataRecorder()
                    )
            }
        }
        return intent?.action?.let { action ->
            Napier.i { "ObservationRecordingService called with intent action: $action" }
            return@let when (action) {
                SERVICE_RECEIVER_START_ACTION -> {
                    intent.getStringArrayListExtra(SCHEDULE_ID)?.let {
                        startObservation(it.toSet())
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

                else -> {
                    START_NOT_STICKY
                }
            }
        } ?: START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Napier.i{ "ObservationRecordingService taskRemove!"}
    }

    override fun onDestroy() {
        Napier.i { "ObservationRecordingService is destroyed!" }
        running = false
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        running = false
        return super.onUnbind(intent)
    }

    private fun startObservation(scheduleId: Set<String>) {
        Napier.i { "Starting the foreground service for scheduleId: $scheduleId..." }
        startForegroundService()
        scope.launch {
            if (StudyRepository().getStudy().firstOrNull()?.active == true) {
                scheduleId.forEach {
                    if (observationManager?.start(it) == true) {
                        runningSchedules.add(it)
                    }
                }
            }
            if (runningSchedules.isEmpty()) {
                stopService()
            }
        }
    }

    private fun pauseObservation(scheduleId: String) {
        observationManager?.pause(scheduleId)
        runningSchedules.remove(scheduleId)
        if (observationManager?.hasRunningTasks() == false) {
            stopService()
        }
    }

    private fun stopObservation(scheduleId: String) {
        observationManager?.stop(scheduleId)
        runningSchedules.remove(scheduleId)
        scheduleRepository.setCompletionStateFor(scheduleId, true)
        if (observationManager?.hasRunningTasks() == false) {
            stopService()
        }
    }

    private fun stopAll() {
        observationManager?.stopAll()
        runningSchedules.clear()
        if (observationManager?.hasRunningTasks() == false) {
            stopService()
        }
    }

    private fun stopService() {
        Napier.i { "Stopping ObservationRecordingService..." }
        stopForeground(STOP_FOREGROUND_REMOVE)
        running = false
        stopSelf()
        Napier.i { "Stopped ObservationRecordingService!" }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Napier.i { "ObservationRecording Service has low memory!" }
    }

    private fun restartAll() {
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

    private fun startForegroundService() {
        Napier.d { "Starting the foreground service..." }
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
        private const val SCHEDULE_ID = "SCHEDULE_ID"
        private const val SERVICE_RECEIVER_START_ACTION =
            "io.redlink.more.app.android.START_SERVICE"
        private const val SERVICE_RECEIVER_PAUSE_ACTION =
            "io.redlink.more.app.android.PAUSE_SERVICE"
        private const val SERVICE_RECEIVER_STOP_ACTION = "io.redlink.more.app.android.STOP_SERVICE"
        private const val SERVICE_RECEIVER_STOP_ALL_ACTION =
            "io.redlink.more.app.android.STOP_ALL_SERVICE"
        private const val SERVICE_RECEIVER_RESTART_ALL_STATES =
            "io.redlink.more.app.android.RESTART_ALL"

        private const val MAX_RETRIES = 100

        var running = false
            private set

        private val runningSchedules = mutableSetOf<String>()


        fun start(
            scheduleIds: Set<String>,
        ) {
            val validToStart = scheduleIds.filter { it !in runningSchedules }
            Scope.launch(Dispatchers.IO) {
                if (!running) {
                    var counter = 0
                    while (MoreApplication.shared?.appIsInForeGround == false) {
                        if (counter++ >= MAX_RETRIES) {
                            log { "Stopping retries for launching observations" }
                            return@launch
                        }
                        log { "Waiting till app goes into foreground to start observations..." }
                        delay(1000)
                    }
                }
                if (validToStart.isNotEmpty() && MoreApplication.shared?.appIsInForeGround == true || running) {
                    val serviceIntent =
                        Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
                    serviceIntent.action = SERVICE_RECEIVER_START_ACTION
                    serviceIntent.putStringArrayListExtra(SCHEDULE_ID, ArrayList(validToStart))
                    try {
                        Handler(Looper.getMainLooper()).post {
                            if (running) {
                                MoreApplication.appContext?.startService(serviceIntent)
                            } else {
                                MoreApplication.appContext?.startForegroundService(serviceIntent)
                            }
                        }
                    } catch (e: Exception) {
                        Napier.e(e.stackTraceToString())
                    }
                } else {
                    Napier.w { "Application not in foreground" }
                }
            }
        }

        fun pause(scheduleId: String) {
            val serviceIntent =
                Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_PAUSE_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            MoreApplication.appContext?.startService(serviceIntent)
        }

        fun stop(scheduleId: String) {
            val serviceIntent =
                Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_STOP_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            MoreApplication.appContext?.startService(serviceIntent)
        }

        fun stopAll() {
            val serviceIntent =
                Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_STOP_ALL_ACTION
            MoreApplication.appContext?.startService(serviceIntent)
        }

        fun restartAll() {
            val serviceIntent =
                Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES
            try {
                Handler(Looper.getMainLooper()).post {
                    MoreApplication.appContext?.startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                Napier.e(e.stackTraceToString())
            }
        }
    }
}