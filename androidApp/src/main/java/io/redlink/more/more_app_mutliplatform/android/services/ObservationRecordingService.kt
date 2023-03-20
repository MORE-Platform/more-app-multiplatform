package io.redlink.more.more_app_mutliplatform.android.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import io.redlink.more.more_app_mutliplatform.android.observations.AndroidObservationFactory
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
private const val TAG = "ObservationRecordingService"
class ObservationRecordingService: Service() {
    private var observationManager: ObservationManager? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (observationManager == null) {
            observationManager = ObservationManager(AndroidObservationFactory(this))
        }
        return intent?.action?.let { action ->
            return@let when (action) {
                SERVICE_RECEIVER_START_ACTION -> {
                    intent.getStringExtra(SCHEDULE_ID)?.let {
                        startObservation(it)
                        return super.onStartCommand(intent, flags, startId)
                    }
                    START_REDELIVER_INTENT
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
                else -> {
                    START_NOT_STICKY
                }
            }
        } ?: START_NOT_STICKY
    }

    private fun startObservation(scheduleId: String) {
        observationManager?.start(scheduleId)
    }

    private fun pauseObservation(scheduleId: String) {
        observationManager?.pause(scheduleId)
    }

    private fun stopObservation(scheduleId: String) {
        observationManager?.stop(scheduleId)
    }

    private fun stopAll() {
        observationManager?.stopAll()
    }

    companion object {
        const val SCHEDULE_ID = "SCHEDULE_ID"
        const val SERVICE_RECEIVER_START_ACTION = "io.redlink.more.app.android.START_SERVICE"
        const val SERVICE_RECEIVER_PAUSE_ACTION = "io.redlink.more.app.android.PAUSE_SERVICE"
        const val SERVICE_RECEIVER_STOP_ACTION = "io.redlink.more.app.android.STOP_SERVICE"
        const val SERVICE_RECEIVER_STOP_ALL_ACTION = "io.redlink.more.app.android.STOP_ALL_SERVICE"

        fun start(
            context: Context,
            scheduleId: String,
        ) {
            val serviceIntent = Intent(context, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_START_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            try {
                Handler(Looper.getMainLooper()).post {
                    context.startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
            }
        }

        fun pause(context: Context, scheduleId: String) {
            val serviceIntent = Intent(context, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_PAUSE_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            context.startService(serviceIntent)
        }

        fun stop(context: Context, scheduleId: String) {
            val serviceIntent = Intent(context, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_STOP_ACTION
            serviceIntent.putExtra(SCHEDULE_ID, scheduleId)
            context.startService(serviceIntent)
        }

        fun stopAll(context: Context) {
            val serviceIntent = Intent(context, ObservationRecordingService::class.java)
            serviceIntent.action = SERVICE_RECEIVER_STOP_ALL_ACTION
            context.startService(serviceIntent)
        }
    }
}