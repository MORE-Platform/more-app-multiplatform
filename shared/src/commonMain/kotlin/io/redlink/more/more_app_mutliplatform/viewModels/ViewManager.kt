package io.redlink.more.more_app_mutliplatform.viewModels

import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ViewManager {
    private val _studyIsUpdating = MutableStateFlow(false)
    private val _showBluetoothView = MutableStateFlow(false)
    val studyIsUpdating: StateFlow<Boolean> = _studyIsUpdating

    val showBluetoothView: StateFlow<Boolean> = _showBluetoothView

    private var bleViewOpen = false


    fun studyIsUpdating(state: Boolean) {
        _studyIsUpdating.set(state)
    }

    fun showBLEView(state: Boolean): Boolean {
        if (!state || !bleViewOpen) {
            _showBluetoothView.set(state)
            return state
        }
        return false
    }

    fun bleViewOpen(state: Boolean) {
        bleViewOpen = state
    }

    fun showBluetoothViewAsClosure(state: (Boolean) -> Unit) = showBluetoothView.asClosure(state)

    fun studyIsUpdatingAsClosure(state: (Boolean) -> Unit) = studyIsUpdating.asClosure(state)

    fun resetAll() {
        _studyIsUpdating.set(false)
        _showBluetoothView.set(false)
    }
}