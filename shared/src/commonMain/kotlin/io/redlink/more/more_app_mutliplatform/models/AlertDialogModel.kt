package io.redlink.more.more_app_mutliplatform.models

data class AlertDialogModel(
    var title: String,
    var message: String,
    var positiveTitle: String,
    var negativeTitle: String? = null,
    var onPositive: () -> Unit = {},
    var onNegative: () -> Unit = {}
)