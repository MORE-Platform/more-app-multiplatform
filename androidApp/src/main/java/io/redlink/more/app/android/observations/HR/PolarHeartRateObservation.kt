package io.redlink.more.app.android.observations.HR

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.services.bluetooth.PolarConnector
import io.redlink.more.more_app_mutliplatform.database.repository.BluetoothDeviceRepository
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        setOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class PolarHeartRateObservation(context: Context) :
    Observation(observationType = PolarVerityHeartRateType(permissions)) {
    private val polarConnector = PolarConnector(context)

    private val hasPermission = this.hasPermissions(context)

    private val bluetoothDeviceRepository = BluetoothDeviceRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val connectedDevices = mutableSetOf<BluetoothDevice>()

    private var heartRateDisposable: Disposable? = null

    init {
        scope.launch {
            bluetoothDeviceRepository.listenForConnectedDevices()
            bluetoothDeviceRepository.connectedDevices.collect { deviceList ->
                connectedDevices.clear()
                val filtered = deviceList.filter { it.deviceName?.lowercase()?.contains("polar") ?: false }
                connectedDevices.addAll(filtered)
            }
        }
    }

    override fun start(): Boolean {
        Napier.d { "Trying to start Polar Verity Heart Rate Observation..." }
        if (connectedDevices.isNotEmpty()) {
            return connectedDevices.first().deviceId?.let {
                try {
                    heartRateDisposable = polarConnector.polarApi.startHrStreaming(it).subscribe(
                        { polarData ->
                            Napier.i { "Polar Data: $polarData" }
                            storeData(mapOf("hr" to polarData.samples[0].hr))
                        },
                        { error ->
                            Napier.e(error.stackTraceToString())
                            stopAndSetState(ScheduleState.PAUSED)
                        })
                    true
                } catch (exception: Exception) {
                    Napier.e { exception.stackTraceToString() }
                    false
                }
            } ?: false
        } else {
            bluetoothDeviceRepository.updateConnectedDevices(10000)
        }
        Napier.d { "No connected devices..." }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        heartRateDisposable?.dispose()
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        return hasPermission
    }

    override fun ableToStart(): String? {
        return super.ableToStart()
    }

    override fun bleDevicesNeeded(): Set<String> {
        return setOf("Polar")
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }

    private fun hasPermissions(context: Context): Boolean {
        permissions.forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    companion object {
        val hrReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
    }
}