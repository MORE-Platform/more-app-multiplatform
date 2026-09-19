/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import io.redlink.more.SharedRes

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

