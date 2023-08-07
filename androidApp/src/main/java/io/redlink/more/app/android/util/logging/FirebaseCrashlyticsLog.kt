package io.redlink.more.app.android.util.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class FirebaseCrashlyticsAntilog : Antilog() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val fullMessage = "$tag: $message"

        if (priority == LogLevel.ERROR || priority == LogLevel.ASSERT) {
            throwable?.let { crashlytics.recordException(it) }
        }

        crashlytics.log("$priority: $fullMessage")
    }
}
