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

package io.redlink.more.services.tracking

import io.redlink.more.logging.track
import io.redlink.more.observations.appUsage.model.LogEvent
import platform.Foundation.NSUserDefaults

private const val PENDING_EVENTS_KEY = "pending_notification_tracking_events"
private const val APP_GROUP = "group.io.redlink.umm.blendedcare.ios"

actual object NotificationTrackingFlusher {
    actual fun flush() {
        val defaults = NSUserDefaults(suiteName = APP_GROUP) ?: return
        val pending = defaults.stringArrayForKey(PENDING_EVENTS_KEY) ?: return
        if (pending.isEmpty()) return
        pending.forEach { _ ->
            LogEvent.NOTIFICATION_DELIVERED.track()
        }
        defaults.removeObjectForKey(PENDING_EVENTS_KEY)
    }
}
