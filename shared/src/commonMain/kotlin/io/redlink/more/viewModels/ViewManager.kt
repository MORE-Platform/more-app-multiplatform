/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */

package io.redlink.more.viewModels

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ViewManager {

    private sealed class ViewRequest {
        object Bluetooth : ViewRequest()
        object GarminConnect : ViewRequest()
    }

    private val pendingViewRequests = ArrayDeque<ViewRequest>()

    private val _studyIsUpdating = MutableStateFlow(false)
    private val _showBluetoothView = MutableStateFlow(false)
    private val _showGarminConnectView = MutableStateFlow(false)
    private val _studyLoadingError = MutableStateFlow(false)
    private val _appInForeground = MutableStateFlow(false)
    private val _bleViewOpen = MutableStateFlow(false)
    private val _activeStudy = MutableStateFlow(false)

    @NativeCoroutines
    val studyLoadingError: StateFlow<Boolean> = _studyLoadingError

    @NativeCoroutines
    val studyIsUpdating: StateFlow<Boolean> = _studyIsUpdating

    @NativeCoroutines
    val bleViewActive: StateFlow<Boolean> = _showBluetoothView

    @NativeCoroutines
    val appInForeground: StateFlow<Boolean> = _appInForeground

    @NativeCoroutines
    val showGarminConnectView: StateFlow<Boolean> = _showGarminConnectView

    @NativeCoroutines
    val activeStudy: StateFlow<Boolean> = _activeStudy

    private fun canOpenNewView(): Boolean {
        return _activeStudy.value &&
                !_studyIsUpdating.value &&
                !_bleViewOpen.value &&
                !_showBluetoothView.value &&
                !_showGarminConnectView.value
    }

    private fun tryProcessNextInQueue() {
        if (!canOpenNewView() || pendingViewRequests.isEmpty()) return

        when (pendingViewRequests.removeFirst()) {
            ViewRequest.Bluetooth -> {
                _showBluetoothView.value = true
            }

            ViewRequest.GarminConnect -> {
                _showGarminConnectView.value = true
            }
        }
    }

    fun currentStudyActive(state: Boolean) {
        _activeStudy.value = state
        if (!state) {
            pendingViewRequests.clear()
            _showBluetoothView.value = false
            _showGarminConnectView.value = false
            _bleViewOpen.value = false
        } else {
            tryProcessNextInQueue()
        }
    }

    fun studyIsUpdating(state: Boolean) {
        _studyIsUpdating.value = state

        if (state) {
            _showBluetoothView.value = false
            _showGarminConnectView.value = false
        } else {
            tryProcessNextInQueue()
        }
    }

    fun showBLEView(state: Boolean): Boolean {
        if (!state) {
            _showBluetoothView.value = false
            return false
        }

        if (canOpenNewView()) {
            _showBluetoothView.value = true
            return true
        } else if (_showBluetoothView.value) {
            return true
        }

        pendingViewRequests.addLast(ViewRequest.Bluetooth)
        return false
    }

    fun bleViewOpen(state: Boolean) {
        _bleViewOpen.value = state
        if (!state) {
            tryProcessNextInQueue()
        }
    }

    fun studyError(hasError: Boolean) {
        _studyLoadingError.value = hasError
    }

    fun appIsInForeground(state: Boolean) {
        _appInForeground.value = state
    }

    fun requestGarminConnectView(state: Boolean): Boolean {
        Napier.d(tag = "ViewManager::requestGarminConnectView") { "Requesting Garmin Connect View: $state" }
        if (!state) {
            _showGarminConnectView.value = false
            tryProcessNextInQueue()
            return false
        }

        if (canOpenNewView()) {
            Napier.d(tag = "ViewManager::requestGarminConnectView") { "Can open Garmin Connect View" }
            _showGarminConnectView.value = true
            return true
        } else if (showGarminConnectView.value) {
            return true
        }

        pendingViewRequests.addLast(ViewRequest.GarminConnect)
        return false
    }

    fun resetAll() {
        _studyIsUpdating.value = false
        _showBluetoothView.value = false
        _showGarminConnectView.value = false
        _studyLoadingError.value = false
        _bleViewOpen.value = false
        pendingViewRequests.clear()
    }
}