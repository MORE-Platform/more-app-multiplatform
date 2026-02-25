package io.redlink.umm.participant.models

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.redlink.umm.participant.SharedRes

enum class NotificationTextKey(val raw: String) {
    OBSERVATION_ACTIVATED("observation_is_active");

    fun asStringDesc(): StringDesc = when (this) {
        OBSERVATION_ACTIVATED -> StringDesc.Resource(SharedRes.strings.observation_is_active)
    }

    fun localize(): StringDesc = asStringDesc()

    companion object {
        fun fromRaw(raw: String?): NotificationTextKey? =
            raw?.let { value -> entries.firstOrNull { it.raw == value } }
    }
}

