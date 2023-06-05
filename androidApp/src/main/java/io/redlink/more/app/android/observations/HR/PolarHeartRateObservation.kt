package io.redlink.more.app.android.observations.HR

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType
import kotlinx.coroutines.flow.MutableStateFlow

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

class PolarHeartRateObservation :
    Observation(observationType = PolarVerityHeartRateType(permissions)) {
    private val deviceIdentifier = setOf("Polar")
    private val polarConnector = MoreApplication.polarConnector!!
    private var heartRateDisposable: Disposable? = null

    override fun start(): Boolean {
        Napier.d { "Trying to start Polar Verity Heart Rate Observation..." }
        val polarDevices = MoreApplication.androidBluetoothConnector!!.connected.filter { it.deviceName?.lowercase()?.contains("polar") ?: false}
        if (polarDevices.isNotEmpty()) {
            return polarDevices.first().deviceId?.let {
                try {
                    heartRateDisposable = polarConnector.polarApi.startHrStreaming(it).subscribe(
                        { polarData ->
                            Napier.i { "Polar Data: $polarData" }
                            storeData(mapOf("hr" to polarData.samples[0].hr))
                        },
                        { error ->
                            Napier.e(error.stackTraceToString())
                            stopAndSetState(ScheduleState.PAUSED, null)
                        })
                    true
                } catch (exception: Exception) {
                    Napier.e { exception.stackTraceToString() }
                    false
                }
            } ?: run {
                Napier.d { "No connected devices..." }
                false
            }
        }
        Napier.d { "No connected devices..." }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        heartRateDisposable?.dispose()
        onCompletion()
    }

    override fun observerAccessible(): Boolean {
        return hasPermissions(MoreApplication.appContext!!)
    }

    override fun bleDevicesNeeded(): Set<String> {
        return deviceIdentifier
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
                Napier.e { "Polar has no bluetooth permissions!" }
                return false
            }
        }
        log { "Polar has Bluetooth Permission!" }
        return true
    }

    companion object {
        val hrReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
    }
}