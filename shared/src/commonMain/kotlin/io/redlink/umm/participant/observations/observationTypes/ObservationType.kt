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
package io.redlink.umm.participant.observations.observationTypes

import io.redlink.umm.participant.database.entities.ObservationDataEntity

open class ObservationType(
    val observationType: String,
    val sensorPermissions: Set<String>,
    val prefix: String? = null,
    val suffix: String? = null,
    val includes: String? = null
) {
    fun addObservationType(schema: ObservationDataEntity): ObservationDataEntity {
        val obsType = observationType
        schema.observationType = obsType
        return schema
    }

    fun matches(type: String): Boolean {
        if (type == observationType) return true
        if (prefix != null && type.startsWith(prefix)) return true
        if (suffix != null && type.endsWith(suffix)) return true
        if (includes != null && type.contains(includes)) return true
        return false
    }

    fun matchesAny(types: Set<String>): Boolean = types.any { matches(it) }
}