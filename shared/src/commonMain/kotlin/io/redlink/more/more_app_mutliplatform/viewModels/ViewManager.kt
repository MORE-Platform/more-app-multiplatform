package io.redlink.more.more_app_mutliplatform.viewModels

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ViewManager {
    private val _studyIsUpdating = MutableStateFlow(false)
    private val _showBluetoothView = MutableStateFlow(false)

    private val _studyLoadingError = MutableStateFlow(false)

    private val _appInForeground = MutableStateFlow(false)

    @NativeCoroutines
    val studyLoadingError: StateFlow<Boolean> = _studyLoadingError

    @NativeCoroutines
    val studyIsUpdating: StateFlow<Boolean> = _studyIsUpdating

    @NativeCoroutines
    val bleViewActive: StateFlow<Boolean> = _showBluetoothView

    @NativeCoroutines
    val appInForeground: StateFlow<Boolean> = _appInForeground

    private val _bleViewOpen = MutableStateFlow(false)

    private val _activeStudy = MutableStateFlow(false)

    fun currentStudyActive(state: Boolean) {
        _activeStudy.value = state
    }

    fun studyIsUpdating(state: Boolean) {
        _studyIsUpdating.value = state
        _showBluetoothView.value = false
    }

    fun showBLEView(state: Boolean): Boolean {
        if (_activeStudy.value && !bleViewActive.value && !_bleViewOpen.value && !_studyIsUpdating.value) {
            _showBluetoothView.value = state
            return state
        }
        _showBluetoothView.value = false
        return false
    }

    fun bleViewOpen(state: Boolean) {
        _bleViewOpen.value = state
    }

    fun studyError(hasError: Boolean) {
        _studyLoadingError.value = hasError
    }

    fun appIsInForeground(state: Boolean) {
        _appInForeground.value = state
    }

    fun resetAll() {
        _studyIsUpdating.value = false
        _showBluetoothView.value = false
        _studyLoadingError.value = false
    }
}