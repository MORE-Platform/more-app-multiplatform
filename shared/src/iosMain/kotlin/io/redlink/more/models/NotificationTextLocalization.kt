/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

import dev.icerock.moko.resources.desc.StringDesc

actual object NotificationTextLocalization {
    actual fun localize(raw: String, fallback: String?): String {
        return localizeToStringDesc(raw)?.localized() ?: fallback ?: raw
    }

    actual fun localizeToStringDesc(raw: String): StringDesc? {
        return NotificationTextKey.fromRaw(raw)?.asStringDesc()
    }
}
