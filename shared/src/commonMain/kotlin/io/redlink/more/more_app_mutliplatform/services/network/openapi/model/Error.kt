@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model


import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Generic Error
 *
 * @param code 
 * @param msg 
 */
@Serializable

data class Error (

    @SerialName(value = "code") val code: kotlin.String? = null,

    @SerialName(value = "msg") val msg: kotlin.String? = null

)

