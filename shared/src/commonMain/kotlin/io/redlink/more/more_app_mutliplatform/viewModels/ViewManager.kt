package io.redlink.more.more_app_mutliplatform.viewModels

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.redlink.more.more_app_mutliplatform.extensions.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ViewManager {
    private val _studyIsUpdating = MutableStateFlow(false)
    private val _showBluetoothView = MutableStateFlow(false)
    private val _checkingForNewStudyData = MutableStateFlow(false)

    @NativeCoroutines
    val studyIsUpdating: StateFlow<Boolean> = _studyIsUpdating

    @NativeCoroutines
    val checkingForNewStudyData: StateFlow<Boolean> = _checkingForNewStudyData

    @NativeCoroutines
    val showBluetoothView: StateFlow<Boolean> = _showBluetoothView

    private var bleViewOpen = false

    fun studyIsUpdating(state: Boolean) {
        _studyIsUpdating.value = state
        _showBluetoothView.value = false
    }

    fun checkingForUpdate(state: Boolean) {
        _checkingForNewStudyData.value = state
    }

    fun showBLEView(state: Boolean): Boolean {
        if (!state || !_showBluetoothView.value || !bleViewOpen || !_studyIsUpdating.value) {
            _showBluetoothView.value = state
            return state
        }
        return false
    }

    fun bleViewOpen(state: Boolean) {
        bleViewOpen = state
    }

    fun resetAll() {
        _studyIsUpdating.set(false)
        _showBluetoothView.set(false)
    }
}