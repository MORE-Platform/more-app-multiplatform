package io.redlink.more.more_app_mutliplatform

import io.redlink.more.more_app_mutliplatform.extensions.asNullableClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.extensions.setNullable
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AlertController {
    private val _alertDialogModel = MutableStateFlow<AlertDialogModel?>(null)

    val alertDialogModel: StateFlow<AlertDialogModel?> = _alertDialogModel
    private var alertDialogQueue = mutableListOf<AlertDialogModel>()

    fun openAlertDialog(model: AlertDialogModel) {
        if (model.onPositive == {}) {
            model.onPositive = {
                closeAlertDialog()
            }
        }
        if (model.onNegative == {}) {
            model.onNegative = {
                closeAlertDialog()
            }
        }
        if (this.alertDialogQueue.isEmpty() && this.alertDialogModel.value == null) {
            this._alertDialogModel.set(model)
        } else {
            this.alertDialogQueue.add(model)
        }
    }

    fun closeAlertDialog() {
        this._alertDialogModel.setNullable(alertDialogQueue.removeFirstOrNull())
    }

    fun onNewAlertDialogModel(provideNewState: ((AlertDialogModel?) -> Unit)) =
        alertDialogModel.asNullableClosure(provideNewState)
}