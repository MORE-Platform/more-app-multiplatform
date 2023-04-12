package io.redlink.more.more_app_mutliplatform.android.extensions

import java.util.*

fun String.formatDateFilterString(): String {
    return this.replace('_', ' ')
        .lowercase(Locale.ROOT)
        .replaceFirstChar { character ->
            character.uppercase() }
}

fun String.formatObservationTypeString(): String {
    return this.replace('-', ' ')
        .replaceFirstChar { character ->
            character.uppercase() }
}