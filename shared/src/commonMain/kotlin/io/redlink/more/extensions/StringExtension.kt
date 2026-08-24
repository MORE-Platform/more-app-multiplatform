/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.extensions

fun String.extractRouteFromDeepLink(): String? {
    val regexPattern = "app://[^/]+/([a-zA-Z0-9-]+)(?:/\\d+)?/?.*".toRegex()
    val matchResult = regexPattern.find(this)
    return matchResult?.groups?.get(1)?.value
}

fun String.decodeURIComponent(): String = this.replace("+", " ").replace("%20", " ")

fun String.mapQueryParams(): Map<String, Set<String>> {
    val queryParams = mutableMapOf<String, MutableSet<String>>()

    val query = this.substringAfter('?', "").substringBefore('#')

    val pairs = query.split("&").filter { it.isNotEmpty() }

    for (pair in pairs) {
        val (key, value) = pair.split("=").map { it.trim().decodeURIComponent() }
        queryParams.getOrPut(key) { mutableSetOf() }.add(value)
    }

    return queryParams.mapValues { it.value.toSet() }
}

fun String.overlaps(other: String?, ignoreCase: Boolean = false): Boolean =
    other?.let { this.contains(other, ignoreCase) || other.contains(this, ignoreCase) } ?: false
