@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model


import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * 
 *
 * @param start 
 * @param end 
 */
@Serializable

data class ObservationSchedule (

    @SerialName(value = "start") val start: LocalDateTime? = null,

    @SerialName(value = "end") val end: LocalDateTime? = null

)

