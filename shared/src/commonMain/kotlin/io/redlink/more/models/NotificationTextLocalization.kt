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
package io.redlink.more.models

import dev.icerock.moko.resources.desc.StringDesc

expect object NotificationTextLocalization {
    fun localize(raw: String, fallback: String? = null): String
    fun localizeToStringDesc(raw: String): StringDesc?
}

fun String.localize(fallback: String? = null): String =
    NotificationTextLocalization.localize(this, fallback ?: this)

fun String.localizeToStringDesc(): StringDesc? =
    NotificationTextLocalization.localizeToStringDesc(this)
