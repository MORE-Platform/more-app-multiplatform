package io.redlink.more.more_app_mutliplatform.viewModels

import io.redlink.more.more_app_mutliplatform.extensions.asNullableClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.extensions.setNullable
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import kotlinx.coroutines.flow.MutableStateFlow

class CoreContentViewModel: CoreViewModel() {
    val alertDialogModel = MutableStateFlow<AlertDialogModel?>(null);
    private var alertDialogQueue = mutableListOf<AlertDialogModel>()

    override fun viewDidAppear() {
    }

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
            this.alertDialogModel.set(model)
        } else {
            this.alertDialogQueue.add(model)
        }
    }

    fun closeAlertDialog() {
        this.alertDialogModel.setNullable(alertDialogQueue.removeFirstOrNull())
    }

    fun onNewAlertDialogModel(provideNewState: ((AlertDialogModel?) -> Unit)) = alertDialogModel.asNullableClosure(provideNewState)
}