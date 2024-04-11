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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import io.redlink.more.more_app_mutliplatform.extensions.urlRegex

fun String.toAnnotatedString(): AnnotatedString {
    val urlStyle = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)
    val builder = AnnotatedString.Builder()

    var lastEndIndex = 0

    urlRegex().findAll(this).forEach { matchResult ->
        builder.append(this.substring(lastEndIndex, matchResult.range.first))

        builder.pushStringAnnotation(tag = "URL", annotation = matchResult.value)
        builder.pushStyle(urlStyle)
        builder.append(matchResult.value)
        builder.pop()
        builder.pop()

        lastEndIndex = matchResult.range.last + 1
    }

    if (lastEndIndex < this.length) {
        builder.append(this.substring(lastEndIndex))
    }

    return builder.toAnnotatedString()
}
