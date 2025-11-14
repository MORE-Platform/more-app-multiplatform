package io.redlink.more.more_app_mutliplatform.extensions

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
