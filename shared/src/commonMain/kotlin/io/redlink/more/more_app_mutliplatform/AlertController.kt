package io.redlink.more.more_app_mutliplatform

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.extensions.setNullable
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AlertController {
    private val _alertDialogModel = MutableStateFlow<AlertDialogModel?>(null)

    @NativeCoroutines
    val alertDialogModel: StateFlow<AlertDialogModel?> = _alertDialogModel
    private var alertDialogQueue = mutableListOf<AlertDialogModel>()

    fun openAlertDialog(model: AlertDialogModel) {
        if (model.onPositive == null || model.onPositive == {}) {
            model.onPositive = {
                closeAlertDialog()
            }
        }
        if (model.onNegative == null || model.onNegative == {}) {
            model.onNegative = {
                closeAlertDialog()
            }
        }
        if (this.alertDialogQueue.isEmpty() && this.alertDialogModel.value == null) {
            this._alertDialogModel.set(model)
        } else if (!this.alertDialogQueue.contains(model) && this.alertDialogModel.value != model) {
            this.alertDialogQueue.add(model)
        }
    }

    fun closeAlertDialog() {
        this._alertDialogModel.setNullable(alertDialogQueue.removeFirstOrNull())
    }
}