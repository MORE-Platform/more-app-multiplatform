package io.redlink.more.more_app_mutliplatform.models

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