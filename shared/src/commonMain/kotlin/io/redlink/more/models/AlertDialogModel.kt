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

package io.redlink.more.models

data class AlertDialogModel(
    var title: String,
    var message: String,
    var confirmLabel: String,
    var cancelLabel: String? = null,
    var onConfirm: (() -> Unit)? = null,
    var onDecline: (() -> Unit)? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AlertDialogModel

        if (title != other.title) return false
        if (message != other.message) return false
        if (confirmLabel != other.confirmLabel) return false
        if (cancelLabel != other.cancelLabel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + confirmLabel.hashCode()
        result = 31 * result + (cancelLabel?.hashCode() ?: 0)
        return result
    }
}