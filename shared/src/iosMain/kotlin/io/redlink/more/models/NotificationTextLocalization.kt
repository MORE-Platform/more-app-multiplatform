package io.redlink.more.models

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc

actual object NotificationTextLocalization {
    actual fun localize(raw: String, fallback: String?): String {
        return localizeToStringDesc(raw)?.localized() ?: fallback ?: StringDesc.Raw(raw).localized()
    }

    actual fun localizeToStringDesc(raw: String): StringDesc? {
        return NotificationTextKey.fromRaw(raw)?.asStringDesc()
    }
}
