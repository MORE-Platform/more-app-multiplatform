package io.redlink.more.more_app_mutliplatform.util

/**
 * Validates and normalizes a URL string by adding https:// scheme if no scheme is present.
 *
 * @return The normalized URL with https:// scheme, or null if the URL is invalid
 */
fun String?.validateAndNormalizeUrl(): String? {
    if (this.isNullOrBlank()) {
        return null
    }

    val trimmedUrl = this.trim()

    return if (trimmedUrl.contains("://")) {
        if (trimmedUrl.isValidUrlFormat()) {
            trimmedUrl
        } else {
            null
        }
    } else {
        val normalizedUrl = "https://$trimmedUrl"
        if (normalizedUrl.isValidUrlFormat()) {
            normalizedUrl
        } else {
            null
        }
    }
}

/**
 * Basic URL format validation
 */
private fun String.isValidUrlFormat(): Boolean {
    return try {
        val schemeIndex = this.indexOf("://")
        if (schemeIndex == -1) return false

        val afterScheme = this.substring(schemeIndex + 3)
        afterScheme.isNotEmpty() && !afterScheme.startsWith("/")
    } catch (e: Exception) {
        false
    }
}