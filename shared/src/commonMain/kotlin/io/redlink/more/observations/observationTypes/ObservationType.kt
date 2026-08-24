/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.observations.observationTypes

open class ObservationType(
    val observationType: String,
    val sensorPermissions: Set<String>,
    val prefix: String? = null,
    val suffix: String? = null,
    val includes: String? = null
) {


    fun matches(type: String): Boolean {
        return type == observationType
                || (prefix != null && type.startsWith(prefix))
                || (suffix != null && type.endsWith(suffix))
                || (includes != null && type.contains(includes))
    }

    fun matchesAny(types: Set<String>): Boolean = types.any { matches(it) }
}