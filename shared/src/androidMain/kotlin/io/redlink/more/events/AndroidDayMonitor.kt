package io.redlink.more.events

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import io.redlink.more.extensions.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

private lateinit var applicationContext: Context

fun initPlatformContext(context: Context) {
    applicationContext = context.applicationContext
}

actual class DayMonitor actual constructor(
    private val onEvent: (AppEvent) -> Unit,
) {
    private var registered = false

    private var lastKnownDate = LocalDate.today()
    private var lastKnownTimeZone = TimeZone.currentSystemDefault()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            when (intent?.action) {
                Intent.ACTION_DATE_CHANGED -> {
                    updateDate()
                }

                Intent.ACTION_TIME_CHANGED -> {
                    onEvent(
                        AppEvent.SystemTimeChanged(
                            Clock.System.now()
                        )
                    )
                    refresh()
                }

                Intent.ACTION_TIMEZONE_CHANGED -> {
                    refresh()
                }
            }
        }
    }

    actual fun start() {
        if (registered) {
            refresh()
            return
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        ContextCompat.registerReceiver(
            applicationContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        registered = true
        refresh()
    }

    actual fun refresh() {
        updateTimeZone()
        updateDate()
    }

    private fun updateDate() {
        val currentDate = LocalDate.today()

        if (currentDate != lastKnownDate) {
            lastKnownDate = currentDate
            onEvent(AppEvent.DayChanged(currentDate))
        }
    }

    private fun updateTimeZone() {
        val currentTimeZone = TimeZone.currentSystemDefault()

        if (currentTimeZone != lastKnownTimeZone) {
            lastKnownTimeZone = currentTimeZone
            onEvent(AppEvent.TimeZoneChanged(currentTimeZone))
        }
    }

    actual fun stop() {
        if (!registered) {
            return
        }

        try {
            applicationContext.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Napier.e(e) {
                "Exception during DayMonitor receiver unregistration"
            }
        } finally {
            registered = false
        }
    }
}
