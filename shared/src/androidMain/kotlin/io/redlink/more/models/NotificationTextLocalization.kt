/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

import android.content.Context
import dev.icerock.moko.resources.desc.StringDesc
import java.lang.ref.WeakReference

actual object NotificationTextLocalization {
    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    actual fun localize(raw: String, fallback: String?): String {
        val context = contextRef?.get()
        return if (context != null) {
            localizeToStringDesc(raw)?.toString(context)
        } else {
            fallback
        } ?: raw
    }

    actual fun localizeToStringDesc(raw: String): StringDesc? {
        return NotificationTextKey.fromRaw(raw)?.asStringDesc()
    }

    fun localize(context: Context, raw: String): String {
        return localizeToStringDesc(raw)?.toString(context) ?: raw
    }
}