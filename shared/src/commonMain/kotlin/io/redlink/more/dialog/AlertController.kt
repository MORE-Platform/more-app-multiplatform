/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.dialog

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.extensions.then
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