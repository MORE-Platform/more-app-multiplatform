package io.redlink.more.models

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.datetime.LocalDate

data class DataDisplayValue(
    val value: String,
    val unit: StringDesc? = null,
    val label: StringDesc? = null,
    val timestamp: LocalDate? = null
)
