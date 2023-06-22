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
 * The study contact object containing all contact information for the study, that can be used when problems occure.
 *
 * @param institute States the institute, that handles the study.
 * @param person States the person a participant can contact, when problems occure. Is required.
 * @param email States the contact email address, that can be written to. Is required.
 * @param phoneNumber States the contact phone number to contact, if added.
 */
@Serializable

data class StudyContact (

    @SerialName(value = "institute") val institute: kotlin.String? = null,

    @SerialName(value = "person") val person: kotlin.String? = null,

    @SerialName(value = "email") val email: kotlin.String? = null,

    @SerialName(value = "phoneNumber") val phoneNumber: kotlin.String? = null,

)

