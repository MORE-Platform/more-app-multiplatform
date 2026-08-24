/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

import io.redlink.more.util.validateAndNormalizeUrl

class LoginModel(
    token: String,
    endpoint: String
) {
    val token: String = token.trim().uppercase()
    val endpoint: String? = endpoint.validateAndNormalizeUrl()

    fun valid(): Boolean = token.isNotBlank() && endpoint != null
}
