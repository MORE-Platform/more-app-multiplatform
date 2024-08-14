package io.redlink.more.more_app_mutliplatform.models

data class AlertDialogModel(
    var title: String,
    var message: String,
    var positiveTitle: String,
    var negativeTitle: String? = null,
    var onPositive: () -> Unit = {},
    var onNegative: () -> Unit = {}
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AlertDialogModel

        if (title != other.title) return false
        if (message != other.message) return false
        if (positiveTitle != other.positiveTitle) return false
        if (negativeTitle != other.negativeTitle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + positiveTitle.hashCode()
        result = 31 * result + (negativeTitle?.hashCode() ?: 0)
        return result
    }
}