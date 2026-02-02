package io.redlink.umm.participant.models

import dev.icerock.moko.resources.desc.StringDesc

expect object NotificationTextLocalization {
    fun localize(raw: String, fallback: String? = null): String
    fun localizeToStringDesc(raw: String): StringDesc?
}

fun String.localize(fallback: String? = null): String =
    NotificationTextLocalization.localize(this, fallback ?: this)

fun String.localizeToStringDesc(): StringDesc? =
    NotificationTextLocalization.localizeToStringDesc(this)
