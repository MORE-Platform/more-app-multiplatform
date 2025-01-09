package io.redlink.more.more_app_mutliplatform.viewModels

import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ViewManager {
    private val _studyIsUpdating = MutableStateFlow(false)
    private val _showBluetoothView = MutableStateFlow(false)
    private val _checkingForNewStudyData = MutableStateFlow(false)
    val studyIsUpdating: StateFlow<Boolean> = _studyIsUpdating
    val checkingForNewStudyData: StateFlow<Boolean> = _checkingForNewStudyData
    val showBluetoothView: StateFlow<Boolean> = _showBluetoothView

    private var bleViewOpen = false

    fun studyIsUpdating(state: Boolean) {
        _studyIsUpdating.set(state)
    }

    fun checkingForUpdate(state: Boolean) {
        _checkingForNewStudyData.set(state)
    }

    fun showBLEView(state: Boolean): Boolean {
        if (!state || !bleViewOpen) {
            _showBluetoothView.update { state }
            return state
        }
        return false
    }

    fun bleViewOpen(state: Boolean) {
        bleViewOpen = state
    }

    fun showBluetoothViewAsClosure(state: (Boolean) -> Unit) = showBluetoothView.asClosure(state)

    fun studyIsUpdatingAsClosure(state: (Boolean) -> Unit) = studyIsUpdating.asClosure(state)

    fun checkingForNewStudyDataAsClosure(state: (Boolean) -> Unit) =
        checkingForNewStudyData.asClosure(state)

    fun resetAll() {
        _studyIsUpdating.set(false)
        _showBluetoothView.set(false)
    }
}