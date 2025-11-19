package io.redlink.umm.participant.models

import io.redlink.umm.participant.util.validateAndNormalizeUrl

class LoginModel(
    token: String,
    endpoint: String
) {
    val token: String = token.trim().uppercase()
    val endpoint: String? = endpoint.validateAndNormalizeUrl()

    fun valid(): Boolean = token.isNotBlank() && endpoint != null
}
