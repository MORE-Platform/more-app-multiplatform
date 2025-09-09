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

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.observations.AndroidDataRecorder
import io.redlink.more.app.android.observations.PermissionUtils
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.app.android.util.ActivityProvider
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ObservationRecordingService : Service() {
    private var observationManager: ObservationManager? = null
    private val scheduleRepository = ScheduleRepository(MoreApplication.shared!!.database)
    private val studyRepository = StudyRepository(MoreApplication.shared!!.database)
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
                        MoreApplication.shared!!.database,
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
                        return START_REDELIVER_INTENT
                    }
                    START_STICKY
                }

                SERVICE_RECEIVER_PAUSE_ACTION -> {
                    intent.getStringExtra(SCHEDULE_ID)?.let {
                        pauseObservation(it)
                    }
                    START_STICKY
                }

                SERVICE_RECEIVER_STOP_ACTION -> {
                    intent.getStringExtra(SCHEDULE_ID)?.let {
                        stopObservation(it)
                    }
                    START_STICKY
                }

                SERVICE_RECEIVER_STOP_ALL_ACTION -> {
                    stopAll()
                    START_NOT_STICKY
                }

                SERVICE_RECEIVER_RESTART_ALL_STATES -> {
                    restartAll()
                    return START_REDELIVER_INTENT
                }

                else -> {
                    START_STICKY
                }
            }
        } ?: START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Napier.i { "ObservationRecordingService taskRemove!" }

        // Restart the service if it's killed when the app is removed from recent apps
        if (runningSchedules.isNotEmpty()) {
            val restartServiceIntent =
                Intent(applicationContext, ObservationRecordingService::class.java)
            restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

            val pendingIntent = PendingIntent.getService(
                applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )

            Napier.i { "Scheduled service restart after task removal" }
        }
    }

    override fun onDestroy() {
        Napier.i { "ObservationRecordingService is destroyed!" }
        running = false

        // Attempt to restart the service if it's destroyed but has running schedules
        if (runningSchedules.isNotEmpty()) {
            Napier.i { "Service destroyed with running schedules, attempting to restart" }
            val restartServiceIntent =
                Intent(applicationContext, ObservationRecordingService::class.java)
            restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

            val pendingIntent = PendingIntent.getService(
                applicationContext, 2, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }

        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        running = false

        // Attempt to restart the service if it's unbound but has running schedules
        if (runningSchedules.isNotEmpty()) {
            Napier.i { "Service unbound with running schedules, attempting to restart" }
            val restartServiceIntent =
                Intent(applicationContext, ObservationRecordingService::class.java)
            restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

            val pendingIntent = PendingIntent.getService(
                applicationContext, 3, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }

        return super.onUnbind(intent)
    }

    private fun startObservation(scheduleId: Set<String>) {
        Napier.i { "Starting the foreground service for scheduleId: $scheduleId..." }
        startForegroundService()
        scope.launch {
            if (studyRepository.getStudy().firstOrNull()?.active == true) {
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
        StudyScope.launch(Dispatchers.IO) {
            scheduleRepository.setCompletionStateFor(scheduleId, true)
        }
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
        if (runningSchedules.isEmpty()) {
            Napier.i { "Stopping ObservationRecordingService..." }
            stopForeground(STOP_FOREGROUND_REMOVE)
            running = false
            stopSelf()
            Napier.i { "Stopped ObservationRecordingService!" }
        } else {
            Napier.i { "Not stopping ObservationRecordingService because there are still running schedules" }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Napier.i { "ObservationRecording Service has low memory!" }

        if (runningSchedules.isNotEmpty()) {
            Napier.i { "Service low on memory with running schedules, attempting to restart" }
            val restartServiceIntent =
                Intent(applicationContext, ObservationRecordingService::class.java)
            restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

            val pendingIntent = PendingIntent.getService(
                applicationContext, 4, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }
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
        val notification = buildNotification(
            channelId = getString(R.string.default_channel_id),
            notificationTitle = getString(R.string.more_observation_running),
            notificationText = getString(R.string.more_observation_notification_explanation)
        )
        startForeground(1001, notification)
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

        private val pendingScheduleIds = mutableSetOf<String>()

        fun start(
            scheduleIds: Set<String>,
        ) {
            val validToStart = scheduleIds.filter { it !in runningSchedules }

            val activity = ActivityProvider.getCurrentActivity()
            if (activity != null && validToStart.isNotEmpty()) {
                checkPermissionsAndStart(validToStart.toSet(), activity)
            } else {
                startWithoutPermissionCheck(validToStart.toSet())
            }
        }

        /**
         * Checks permissions before starting observations
         * @param scheduleIds The schedule IDs to start
         * @param activity The activity to request permissions in
         */
        private fun checkPermissionsAndStart(
            scheduleIds: Set<String>,
            activity: android.app.Activity
        ) {
            val observations =
                MoreApplication.shared?.observationFactory?.observations ?: emptySet()

            if (observations.isEmpty()) {
                startWithoutPermissionCheck(scheduleIds)
                return
            }

            val allPermissionsGranted = observations.all { observation ->
                PermissionUtils.hasAllPermissions(observation, activity)
            }

            if (allPermissionsGranted) {
                startWithoutPermissionCheck(scheduleIds)
            } else {
                val observationNeedingPermissions = observations.firstOrNull { observation ->
                    !PermissionUtils.hasAllPermissions(observation, activity)
                }

                if (observationNeedingPermissions != null) {
                    pendingScheduleIds.addAll(scheduleIds)

                    PermissionUtils.requestPermissions(
                        observationNeedingPermissions,
                        activity
                    ) { granted ->
                        if (granted) {
                            checkPermissionsAndStart(scheduleIds, activity)
                        } else {
                            observationNeedingPermissions.showPermissionAlertDialog()
                            pendingScheduleIds.removeAll(scheduleIds)
                        }
                    }
                } else {
                    startWithoutPermissionCheck(scheduleIds)
                }
            }
        }

        /**
         * Starts observations without permission check
         * @param scheduleIds The schedule IDs to start
         */
        private fun startWithoutPermissionCheck(scheduleIds: Set<String>) {
            Scope.launch(Dispatchers.IO) {
                // Always start observations regardless of foreground/background state
                if (scheduleIds.isNotEmpty()) {
                    val serviceIntent =
                        Intent(MoreApplication.appContext, ObservationRecordingService::class.java)
                    serviceIntent.action = SERVICE_RECEIVER_START_ACTION
                    serviceIntent.putStringArrayListExtra(SCHEDULE_ID, ArrayList(scheduleIds))
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
