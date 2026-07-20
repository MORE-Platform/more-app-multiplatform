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

package io.redlink.more.dialog

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc

data class AlertDialogModel(
    var title: StringDesc,
    var message: StringDesc,
    var confirmLabel: StringDesc,
    var cancelLabel: StringDesc? = null,
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

    companion object {
        fun fromStrings(
            title: String,
            message: String,
            confirmLabel: String,
            cancelLabel: String? = null,
            onConfirm: (() -> Unit)? = null,
            onDecline: (() -> Unit)? = null
        ): AlertDialogModel {
            return AlertDialogModel(
                StringDesc.Raw(title),
                StringDesc.Raw(message),
                StringDesc.Raw(confirmLabel),
                cancelLabel?.let { StringDesc.Raw(it) },
                onConfirm,
                onDecline
            )
        }
    }
}