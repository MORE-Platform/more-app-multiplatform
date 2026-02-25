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
