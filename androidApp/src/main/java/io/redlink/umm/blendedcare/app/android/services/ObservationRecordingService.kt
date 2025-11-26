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
package io.redlink.umm.blendedcare.app.android.services

import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.activities.ContentActivity
import io.redlink.umm.blendedcare.app.android.observations.PermissionUtils
import io.redlink.umm.blendedcare.app.android.observations.showPermissionAlertDialog
import io.redlink.umm.blendedcare.app.android.util.ActivityProvider
import io.redlink.umm.participant.observations.ObservationFactory
import io.redlink.umm.participant.observations.ObservationManager
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.scopes.StudyScope
import io.redlink.umm.participant.viewModels.ViewManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ObservationRecordingService : Service() {
    private var observationManager: ObservationManager? = null
    private var observationFactory: ObservationFactory? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Napier.i { "ObservationRecordingService called..." }

        // Critical: Start foreground service immediately to prevent ANR crashes
        // This must happen within 5 seconds when startForegroundService() is called
        try {
            if (!running) {
                startForegroundService()
            }
        } catch (e: Exception) {
            Napier.e("Failed to start foreground service: ${e.message}")
            try {
                val basicNotification = Notification.Builder(this, "default")
                    .setContentTitle("More Observation Service")
                    .setContentText("Service is running")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build()
                startForeground(1001, basicNotification)
                running = true
            } catch (fallbackException: Exception) {
                Napier.e("Failed to start foreground service with fallback notification: ${fallbackException.message}")
                stopSelf()
                return START_NOT_STICKY
            }
        }

        if (observationFactory == null) {
            if (BlendedCareApplication.shared == null) {
                BlendedCareApplication.initShared(applicationContext)
            }
            observationFactory = BlendedCareApplication.shared!!.observationFactory
        }
        observationFactory?.let {
            if (observationManager == null) {
                observationManager = BlendedCareApplication.shared!!.observationManager
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

        try {
            if (runningSchedules.isNotEmpty()) {
                val restartServiceIntent =
                    Intent(applicationContext, ObservationRecordingService::class.java)
                restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

                val pendingIntent = PendingIntent.getService(
                    applicationContext, 1, restartServiceIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager =
                    applicationContext.getSystemService(ALARM_SERVICE) as? AlarmManager
                alarmManager?.let {
                    try {
                        it.set(
                            AlarmManager.ELAPSED_REALTIME,
                            SystemClock.elapsedRealtime() + 1000,
                            pendingIntent
                        )
                        Napier.i { "Scheduled service restart after task removal" }
                    } catch (e: SecurityException) {
                        Napier.e("Failed to schedule restart due to security restriction: ${e.message}")
                    } catch (e: Exception) {
                        Napier.e("Failed to schedule service restart: ${e.message}")
                    }
                } ?: Napier.e("AlarmManager not available for service restart")
            }
        } catch (e: Exception) {
            Napier.e("Error in onTaskRemoved: ${e.message}")
        }
    }

    override fun onDestroy() {
        Napier.i { "ObservationRecordingService is destroyed!" }
        running = false

        try {
            try {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } catch (e: Exception) {
                Napier.e("Failed to stop foreground service: ${e.message}")
            }

            if (runningSchedules.isNotEmpty()) {
                Napier.i { "Service destroyed with running schedules, attempting to restart" }
                val restartServiceIntent =
                    Intent(applicationContext, ObservationRecordingService::class.java)
                restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

                val pendingIntent = PendingIntent.getService(
                    applicationContext, 2, restartServiceIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager =
                    applicationContext.getSystemService(ALARM_SERVICE) as? AlarmManager
                alarmManager?.let {
                    try {
                        it.set(
                            AlarmManager.ELAPSED_REALTIME,
                            SystemClock.elapsedRealtime() + 1000,
                            pendingIntent
                        )
                        Napier.i { "Scheduled service restart from onDestroy" }
                    } catch (e: SecurityException) {
                        Napier.e("Failed to schedule restart due to security restriction: ${e.message}")
                    } catch (e: Exception) {
                        Napier.e("Failed to schedule service restart from onDestroy: ${e.message}")
                    }
                } ?: Napier.e("AlarmManager not available for service restart from onDestroy")
            }
        } catch (e: Exception) {
            Napier.e("Error in onDestroy: ${e.message}")
        } finally {
            try {
                super.onDestroy()
            } catch (e: Exception) {
                Napier.e("Error in super.onDestroy(): ${e.message}")
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        running = false

        try {
            if (runningSchedules.isNotEmpty()) {
                Napier.i { "Service unbound with running schedules, attempting to restart" }
                val restartServiceIntent =
                    Intent(applicationContext, ObservationRecordingService::class.java)
                restartServiceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES

                val pendingIntent = PendingIntent.getService(
                    applicationContext, 3, restartServiceIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager =
                    applicationContext.getSystemService(ALARM_SERVICE) as? AlarmManager
                alarmManager?.let {
                    try {
                        it.set(
                            AlarmManager.ELAPSED_REALTIME,
                            SystemClock.elapsedRealtime() + 1000,
                            pendingIntent
                        )
                        Napier.i { "Scheduled service restart from onUnbind" }
                    } catch (e: SecurityException) {
                        Napier.e("Failed to schedule restart due to security restriction: ${e.message}")
                    } catch (e: Exception) {
                        Napier.e("Failed to schedule service restart from onUnbind: ${e.message}")
                    }
                } ?: Napier.e("AlarmManager not available for service restart from onUnbind")
            }
        } catch (e: Exception) {
            Napier.e("Error in onUnbind: ${e.message}")
        }

        return try {
            super.onUnbind(intent)
        } catch (e: Exception) {
            Napier.e("Error in super.onUnbind(): ${e.message}")
            false
        }
    }

    private fun startObservation(scheduleId: Set<String>) {
        Napier.i { "Starting the foreground service for scheduleId: $scheduleId..." }
        try {
            startForegroundService()
            scope.launch {
                try {
                    if (BlendedCareApplication.shared!!.repositories.study.study.value?.active == true) {
                        scheduleId.forEach { id ->
                            try {
                                if (observationManager?.start(id) == true) {
                                    runningSchedules.add(id)
                                    Napier.d { "Successfully started observation for schedule: $id" }
                                } else {
                                    Napier.w { "Failed to start observation for schedule: $id" }
                                }
                            } catch (e: Exception) {
                                Napier.e("Error starting observation for schedule $id: ${e.message}")
                            }
                        }
                    } else {
                        Napier.w { "Study is not active, skipping observation start" }
                    }

                    if (runningSchedules.isEmpty()) {
                        Napier.i { "No observations started, stopping service" }
                        stopService()
                    }
                } catch (e: Exception) {
                    Napier.e("Error in startObservation coroutine: ${e.message}")
                    if (runningSchedules.isEmpty()) {
                        stopService()
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e("Error starting observation: ${e.message}")
            try {
                stopService()
            } catch (stopError: Exception) {
                Napier.e("Error stopping service after startup failure: ${stopError.message}")
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
            BlendedCareApplication.shared!!.repositories.schedule.setCompletionStateFor(
                scheduleId,
                true
            )
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
                applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }
    }

    private fun restartAll() {
        try {
            startForegroundService()
            scope.launch {
                try {
                    Napier.i { "Restarting all running observations..." }
                    val startedObservations =
                        observationManager?.restartStillRunning() ?: emptySet()

                    if (startedObservations.isNotEmpty()) {
                        runningSchedules.clear()
                        runningSchedules.addAll(startedObservations)
                        Napier.i { "Restarted ${startedObservations.size} observations: $startedObservations" }
                    } else {
                        Napier.i { "No observations to restart" }
                        val hasRunningTasks = try {
                            observationManager?.hasRunningTasks() == true
                        } catch (e: Exception) {
                            Napier.e("Error checking running tasks: ${e.message}")
                            false
                        }

                        if (!hasRunningTasks) {
                            Napier.i { "No running tasks found, stopping service" }
                            stopAll()
                        }
                    }
                } catch (e: Exception) {
                    Napier.e("Error in restartAll coroutine: ${e.message}")
                    try {
                        if (runningSchedules.isEmpty() && observationManager?.hasRunningTasks() != true) {
                            Napier.i { "No active observations after restart error, stopping service" }
                            stopAll()
                        }
                    } catch (stopError: Exception) {
                        Napier.e("Error handling restart failure: ${stopError.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e("Error starting foreground service in restartAll: ${e.message}")
            try {
                stopAll()
            } catch (stopError: Exception) {
                Napier.e("Error stopping service after restartAll failure: ${stopError.message}")
            }
        }
    }

    private fun startForegroundService() {
        Napier.d { "Starting the foreground service..." }
        try {
            val channelId = try {
                getString(R.string.default_channel_id)
            } catch (e: Exception) {
                "default"
            }

            val notificationTitle = try {
                getString(R.string.more_observation_running)
            } catch (e: Exception) {
                "More Observation Service"
            }

            val notificationText = try {
                getString(R.string.more_observation_notification_explanation)
            } catch (e: Exception) {
                "Service is running"
            }

            val notification = buildNotification(
                channelId = channelId,
                notificationTitle = notificationTitle,
                notificationText = notificationText
            )
            startForeground(1001, notification)
            running = true
            Napier.d { "Foreground service started successfully" }
        } catch (e: Exception) {
            Napier.e("Failed to start foreground service: ${e.message}")
            throw e
        }
    }

    private fun buildNotification(
        channelId: String,
        notificationTitle: String,
        notificationText: String,
    ): Notification {
        try {
            val channel = NotificationChannel(
                channelId,
                channelId,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "More observation service notifications"
                enableLights(false)
                enableVibration(false)
            }

            val intent = Intent(this, ContentActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)

            return Notification.Builder(applicationContext, channelId)
                .setContentText(notificationText)
                .setContentTitle(notificationTitle)
                .setSmallIcon(R.mipmap.ic_more_logo_hf_v2)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()
        } catch (e: Exception) {
            Napier.e("Failed to build notification: ${e.message}")
            return Notification.Builder(applicationContext, "default")
                .setContentTitle("More Service")
                .setContentText("Running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build()
        }
    }

    companion object {
        private const val SCHEDULE_ID = "SCHEDULE_ID"
        private const val SERVICE_RECEIVER_START_ACTION =
            "io.redlink.umm.blendedcare.app.android.START_SERVICE"
        private const val SERVICE_RECEIVER_PAUSE_ACTION =
            "io.redlink.umm.blendedcare.app.android.PAUSE_SERVICE"
        private const val SERVICE_RECEIVER_STOP_ACTION =
            "io.redlink.umm.blendedcare.app.android.STOP_SERVICE"
        private const val SERVICE_RECEIVER_STOP_ALL_ACTION =
            "io.redlink.umm.blendedcare.app.android.STOP_ALL_SERVICE"
        private const val SERVICE_RECEIVER_RESTART_ALL_STATES =
            "io.redlink.umm.blendedcare.app.android.RESTART_ALL"

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
            activity: Activity
        ) {
            val observations =
                BlendedCareApplication.shared?.observationFactory?.observations
                    ?: emptySet()

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
                if (scheduleIds.isNotEmpty()) {
                    val serviceIntent =
                        Intent(
                            BlendedCareApplication.appContext,
                            ObservationRecordingService::class.java
                        )
                    serviceIntent.action = SERVICE_RECEIVER_START_ACTION
                    serviceIntent.putStringArrayListExtra(SCHEDULE_ID, ArrayList(scheduleIds))
                    try {
                        Handler(Looper.getMainLooper()).post {
                            if (running) {
                                BlendedCareApplication.appContext?.startService(
                                    serviceIntent
                                )
                            } else {
                                BlendedCareApplication.appContext?.startForegroundService(
                                    serviceIntent
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Napier.e(e.stackTraceToString())
                    }
                }
            }
        }

        fun pause(scheduleId: String) {
            if (ViewManager.appInForeground.value) {
                val serviceIntent =
                    Intent(
                        BlendedCareApplication.appContext,
                        ObservationRecordingService::class.java
                    )
                serviceIntent.action = SERVICE_RECEIVER_PAUSE_ACTION
                serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
                BlendedCareApplication.appContext?.startService(serviceIntent)
            } else {
                BlendedCareApplication.shared?.observationManager?.pause(scheduleId)
            }
        }

        fun stop(scheduleId: String) {
            if (ViewManager.appInForeground.value) {
                val serviceIntent =
                    Intent(
                        BlendedCareApplication.appContext,
                        ObservationRecordingService::class.java
                    )
                serviceIntent.action = SERVICE_RECEIVER_STOP_ACTION
                serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
                BlendedCareApplication.appContext?.startService(serviceIntent)
            } else {
                BlendedCareApplication.shared?.observationManager?.stop(scheduleId)
            }
        }

        fun stopAll() {
            if (ViewManager.appInForeground.value) {
                val serviceIntent =
                    Intent(
                        BlendedCareApplication.appContext,
                        ObservationRecordingService::class.java
                    )
                serviceIntent.action = SERVICE_RECEIVER_STOP_ALL_ACTION
                BlendedCareApplication.appContext?.startService(serviceIntent)
            } else {
                BlendedCareApplication.shared?.observationManager?.stopAll()
            }
        }

        fun restartAll() {
            val serviceIntent =
                Intent(
                    BlendedCareApplication.appContext,
                    ObservationRecordingService::class.java
                )
            serviceIntent.action = SERVICE_RECEIVER_RESTART_ALL_STATES
            try {
                Handler(Looper.getMainLooper()).post {
                    BlendedCareApplication.appContext?.startForegroundService(
                        serviceIntent
                    )
                }
            } catch (e: Exception) {
                Napier.e(e.stackTraceToString())
            }
        }
    }
}
