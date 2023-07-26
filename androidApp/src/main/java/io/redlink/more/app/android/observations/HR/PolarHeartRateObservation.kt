package io.redlink.more.app.android.observations.HR

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.reactivex.rxjava3.disposables.Disposable
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.PolarVerityHeartRateType
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.delay
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
        if (observerAccessible()) {
            val polarDevices = MoreApplication.shared!!.mainBluetoothConnector.connected.filter {
                it.deviceName?.lowercase()?.contains("polar") ?: false
            }
            return polarDevices.first().deviceId?.let {
                try {
                    heartRateDisposable = polarConnector.polarApi.startHrStreaming(it).subscribe(
                        { polarData ->
                            Napier.i { "Polar Data: $polarData" }
                            storeData(mapOf("hr" to polarData.samples[0].hr))
                        },
                        { error ->
                            Napier.e("HR Recording error: ${error.stackTraceToString()}")
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
        val empty = MoreApplication.shared!!.mainBluetoothConnector.connected.isEmpty()
        if (empty) {
            MoreApplication.shared!!.coreBluetooth.enableBackgroundScanner()
        } else {
            MoreApplication.shared!!.coreBluetooth.disableBackgroundScanner()
        }
        return hasPermissions(MoreApplication.appContext!!) && !empty
    }

    override fun bleDevicesNeeded(): Set<String> {
        return deviceIdentifier
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return observerAccessible()
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
        fun setHRReady(ready: Boolean) {
            hrReady.set(ready)
            if (ready) {
                MoreApplication.shared!!.observationManager.startObservationType(
                    PolarVerityHeartRateType(
                        emptySet()
                    ).observationType
                )
            } else {
                MoreApplication.shared!!.observationManager.pauseObservationType(
                    PolarVerityHeartRateType(
                        emptySet()
                    ).observationType
                )
            }
        }
    }
}