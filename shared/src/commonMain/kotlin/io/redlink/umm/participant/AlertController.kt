package io.redlink.umm.participant

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.umm.participant.extensions.then
import io.redlink.umm.participant.models.AlertDialogModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AlertController {
    private val _alertDialogModel = MutableStateFlow<AlertDialogModel?>(null)

    @NativeCoroutines
    val alertDialogModel: StateFlow<AlertDialogModel?> = _alertDialogModel
    private var alertDialogQueue = mutableListOf<AlertDialogModel>()

    fun openAlertDialog(model: AlertDialogModel) {
        model.onConfirm = composeWithClose(model.onConfirm)
        model.onDecline = composeWithClose(model.onDecline)

        if (this.alertDialogQueue.isEmpty() && this.alertDialogModel.value == null) {
            this._alertDialogModel.value = model
        } else if (!this.alertDialogQueue.contains(model) && this.alertDialogModel.value != model) {
            this.alertDialogQueue.add(model)
        }
    }

    private fun composeWithClose(action: (() -> Unit)?): () -> Unit =
        (action ?: {}) then { closeAlertDialog() }

    fun closeAlertDialog() {
        this._alertDialogModel.value = alertDialogQueue.removeFirstOrNull()
    }
}