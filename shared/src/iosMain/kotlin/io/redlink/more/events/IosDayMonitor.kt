package io.redlink.more.events

import io.redlink.more.extensions.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import platform.Foundation.NSCalendarDayChangedNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSystemClockDidChangeNotification
import platform.Foundation.NSSystemTimeZoneDidChangeNotification
import kotlin.time.Clock

actual class DayMonitor actual constructor(
    private val onEvent: (AppEvent) -> Unit
) {
    private val notificationCenter =
        NSNotificationCenter.defaultCenter

    private var calendarObserver: Any? = null
    private var clockObserver: Any? = null
    private var timezoneObserver: Any? = null

    private var lastKnownDate = LocalDate.today()
    private var lastKnownTimeZone = TimeZone.currentSystemDefault()

    actual fun start() {
        if (calendarObserver != null) {
            return
        }

        calendarObserver =
            notificationCenter.addObserverForName(
                name = NSCalendarDayChangedNotification,
                `object` = null,
                queue = null
            ) {
                onEvent(AppEvent.DayChanged(LocalDate.today()))
            }

        clockObserver =
            notificationCenter.addObserverForName(
                name = NSSystemClockDidChangeNotification,
                `object` = null,
                queue = null
            ) {
                onEvent(AppEvent.SystemTimeChanged(Clock.System.now()))
            }

        timezoneObserver =
            notificationCenter.addObserverForName(
                name = NSSystemTimeZoneDidChangeNotification,
                `object` = null,
                queue = null
            ) {
                onEvent(AppEvent.TimeZoneChanged(TimeZone.currentSystemDefault()))
            }
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
        calendarObserver?.let {
            notificationCenter.removeObserver(it)
        }

        clockObserver?.let {
            notificationCenter.removeObserver(it)
        }

        timezoneObserver?.let {
            notificationCenter.removeObserver(it)
        }

        calendarObserver = null
        clockObserver = null
        timezoneObserver = null
    }
}
