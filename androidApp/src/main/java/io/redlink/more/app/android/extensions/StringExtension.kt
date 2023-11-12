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
package io.redlink.more.app.android.extensions

import android.net.Uri

fun String.extractKeyValuePairs(): Map<String, String> {
    val keyValuePairs = mutableMapOf<String, String>()

    val pairs = this.split("&")

    for (pair in pairs) {
        val keyValue = pair.split("=")
        if (keyValue.size == 2) {
            val key = Uri.decode(keyValue[0])
            val value = Uri.decode(keyValue[1])

            keyValuePairs[key] = value
        }
    }

    return keyValuePairs
}