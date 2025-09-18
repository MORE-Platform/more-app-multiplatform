package io.redlink.more.more_app_mutliplatform.util

/**
 * Validates and normalizes a URL string by adding https:// scheme if no scheme is present
 * and removing trailing slash if present.
 *
 * @return The normalized URL with https:// scheme and no trailing slash, or null if the URL is invalid
 */
fun String?.validateAndNormalizeUrl(): String? {
    if (this.isNullOrBlank()) {
        return null
    }

    var trimmedUrl = this.trim()

    if (!trimmedUrl.contains("://")) {
        trimmedUrl = "https://$trimmedUrl"
    }

    if (!trimmedUrl.isValidUrlFormat()) {
        return null
    }

    return if (trimmedUrl.endsWith("/") && trimmedUrl.count { it == '/' } > 2) {
        trimmedUrl.dropLast(1)
    } else {
        trimmedUrl
    }
}

/**
 * Enhanced URL format validation that checks scheme, domain, and structure
 */
private fun String.isValidUrlFormat(): Boolean {
    return try {
        val schemeIndex = this.indexOf("://")
        if (schemeIndex == -1) return false

        val scheme = this.substring(0, schemeIndex).lowercase()
        if (scheme !in listOf("http", "https", "ftp", "ftps")) return false

        val afterScheme = this.substring(schemeIndex + 3)
        if (afterScheme.isEmpty() || afterScheme.startsWith("/")) return false

        val pathIndex = afterScheme.indexOf('/')
        val hostPart = if (pathIndex == -1) afterScheme else afterScheme.substring(0, pathIndex)

        if (!hostPart.isValidHostPart()) return false

        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Validates the host part of a URL (domain with optional port)
 */
private fun String.isValidHostPart(): Boolean {
    if (this.isEmpty()) return false

    val parts = this.split(':')
    if (parts.size > 2) return false

    val host = parts[0]
    val port = if (parts.size == 2) parts[1] else null

    if (!host.isValidDomain()) return false

    if (port != null) {
        val portNumber = port.toIntOrNull()
        if (portNumber == null || portNumber < 1 || portNumber > 65535) return false
    }

    return true
}

/**
 * Basic domain name validation
 */
private fun String.isValidDomain(): Boolean {
    if (this.isEmpty() || this.length > 253) return false
    if (this.startsWith(".") || this.endsWith(".")) return false
    if (this.contains("..")) return false

    val labels = this.split('.')
    if (labels.isEmpty()) return false

    for (label in labels) {
        if (label.isEmpty() || label.length > 63) return false
        if (label.startsWith("-") || label.endsWith("-")) return false
        if (!label.all { it.isLetterOrDigit() || it == '-' }) return false
    }

    return true
}