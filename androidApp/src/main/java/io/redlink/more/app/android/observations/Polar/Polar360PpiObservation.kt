package io.redlink.more.app.android.observations.Polar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarPpiData
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.observations.pauseObservation
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.anyNameIn
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.Polar360PpiType
import io.redlink.more.scopes.Scope
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import kotlinx.coroutines.Job

private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (Build.VERSION.SDK_INT >= 34) {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
            )
        } else {
            setOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    } else {
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

class Polar360PpiObservation(repos: MainRepository) :
    Observation(repos, observationType = Polar360PpiType(permissions)) {

    private val deviceIdentifier = setOf("Polar")
    private var ppiDisposable: Disposable? = null
    private var offlineRecordingDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null
    private var offlineMode = false

    companion object {
        var recording_startTimestamp: Long? = null
        var recroding_endTimestamp: Long? = null

        fun padToOneHz(
            samples: List<ppi_data>,
            startNs: Long,
            endNs: Long
        ): List<ppi_data> {
            if (endNs <= 0 || samples.isEmpty()) return samples

            val step = 1_000_000_000L
            val sorted = samples.sortedBy { it.timestamp }

            // Window start: take the earlier of recording_startTimestamp and first sample,
            // floored to a whole second, so the full recording period is covered.
            val firstSampleTs = sorted.first().timestamp
            val rawStart = if (startNs > 0) minOf(startNs, firstSampleTs) else firstSampleTs
            val effectiveStart = (rawStart / step) * step

            if (effectiveStart >= endNs) return samples

            val maxDurationNs = 24L * 3600 * 1_000_000_000L
            if (endNs - effectiveStart > maxDurationNs) {
                Napier.w(tag = "Polar360PpiObservation") { "padToOneHz — derived duration exceeds 24 h cap; returning raw samples" }
                return samples
            }

            val result = mutableListOf<ppi_data>()
            var sampleIndex = 0
            var cursor = effectiveStart

            while (cursor < endNs) {
                val slotEnd = cursor + step
                // Advance past any samples that fall before this slot.
                while (sampleIndex < sorted.size && sorted[sampleIndex].timestamp < cursor) {
                    sampleIndex++
                }
                if (sampleIndex < sorted.size && sorted[sampleIndex].timestamp < slotEnd) {
                    val s = sorted[sampleIndex]
                    result.add(ppi_data(hr = s.hr, timestamp = cursor, ppiInMs = s.ppiInMs, ppiErrorEstimate = s.ppiErrorEstimate, skinContact = s.skinContact))
                    sampleIndex++
                    // Skip any additional samples within the same 1-second slot.
                    while (sampleIndex < sorted.size && sorted[sampleIndex].timestamp < slotEnd) {
                        sampleIndex++
                    }
                } else {
                    result.add(ppi_data(hr = -99, timestamp = cursor, ppiInMs = 0, ppiErrorEstimate = 65535, skinContact = false))
                }
                cursor = slotEnd
            }

            val validCount = result.count { it.ppiErrorEstimate != 65535 }
            Napier.d(tag = "Polar360PpiObservation") { "padToOneHz — total=${result.size}, valid=$validCount, corrupted=${result.size - validCount}" }
            return result
        }
    }

    override fun start(): Boolean {
        Napier.d(tag = "Polar360PpiObservation::start") { "Starting Polar 360 PPI (offline=$offlineMode)..." }
        if (!observerAccessible()) {
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val device = Polar360Controller.findPolar360Device()
        if (device == null) {
            Napier.d(tag = "Polar360PpiObservation::start") { "No Polar 360 device connected" }
            showObservationErrorNotification(
                stringResource(R.string.observation_cannot_start),
                stringResource(R.string.observation_error)
            )
            return false
        }

        val deviceId = device.deviceId!!
        Polar360Controller.ensureReady(deviceId, offlineMode = offlineMode,
            onReady = {
                if (offlineMode) {
                    Polar360Controller.stopOfflineRecording(PolarBleApi.PolarDeviceDataType.PPI)
                        .subscribe(
                            { items ->
                                val processed = processPpiSamples(items.filterIsInstance<PolarPpiData.PolarPpiSample>())
                                if (processed.isNotEmpty()) {
                                    val padded = padToOneHz(
                                        samples = processed,
                                        startNs = recording_startTimestamp ?: 0L,
                                        endNs = recroding_endTimestamp ?: 0L
                                    )
                                    storeData(mapOf("polar360ppidata" to padded), -1) {}
                                }
                                offlineRecordingDisposable = Polar360Controller.startOfflineRecording(
                                    deviceId, PolarBleApi.PolarDeviceDataType.PPI
                                )
                            },
                            { error ->
                                Napier.e(tag = "Polar360PpiObservation") { "Failed to fetch pending offline data: ${error.message}" }
                                offlineRecordingDisposable = Polar360Controller.startOfflineRecording(
                                    deviceId, PolarBleApi.PolarDeviceDataType.PPI
                                )
                            }
                        )
                } else {
                    ppiDisposable = Polar360Controller.getPolarApi()
                        .startPpiStreaming(deviceId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            { data: PolarPpiData ->
                                if (data.samples.isNotEmpty()) {
                                    val processed = processPpiSamples(data.samples)
                                    processed.forEach { storeData(it) }
                                }
                            },
                            { error ->
                                Napier.e(tag = "Polar360PpiObservation") { "PPI stream failed: ${error.message}" }
                                ppiDisposable = null
                            }
                        )
                }
            },
            onError = { error ->
                Napier.e(tag = "Polar360PpiObservation") { "Setup failed: ${error.message}" }
                showObservationErrorNotification(
                    stringResource(R.string.observation_cannot_start),
                    stringResource(R.string.observation_error)
                )
            }
        )

        deviceConnectionListener = listenToDeviceConnection()
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        deviceConnectionListener?.cancel()
        deviceConnectionListener = null
        if (offlineMode) {
            offlineRecordingDisposable?.dispose()
            offlineRecordingDisposable = null
            Polar360Controller.stopOfflineRecording(PolarBleApi.PolarDeviceDataType.PPI)
                .subscribe(
                    { items ->
                        val processed = processPpiSamples(items.filterIsInstance<PolarPpiData.PolarPpiSample>())
                        val padded = padToOneHz(
                            samples = processed,
                            //todo polar uses non traditional timestamp start date
                            startNs = recording_startTimestamp ?: 0L,
                            endNs = recroding_endTimestamp ?: 0L
                        )
                        storeData(mapOf("polar360ppidata" to padded), -1, onCompletion)
                    },
                    { error ->
                        Napier.e(tag = "Polar360PpiObservation") { "Failed to process offline data: ${error.message}" }
                        onCompletion()
                    }
                )
        } else {
            ppiDisposable?.dispose()
            ppiDisposable = null
            onCompletion()
        }
    }

    data class ppi_data(val hr: Int, val timestamp: Long, val ppiInMs: Int, val ppiErrorEstimate: Int , var skinContact: Boolean)

    fun processPpiSamples(samples: List<PolarPpiData.PolarPpiSample>?): List<ppi_data> {
        if (samples.isNullOrEmpty()) return emptyList()
        return samples.map {
            ppi_data(hr = it.hr, timestamp = it.timeStamp.toLong(), ppiInMs = it.ppi, ppiErrorEstimate = it.errorEstimate , skinContact =  it.skinContactStatus )
        }
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (!hasPermissions(MoreApplication.appContext!!)) {
            errors.add("error_access_bluetooth")
            showPermissionAlertDialog()
        }
        if (!BluetoothStateListener.bluetoothEnabled.value) {
            errors.add("bluetooth_disabled")
        }
        if (!MoreApplication.shared!!.bluetoothController.observerDeviceAccessible(deviceIdentifier)) {
            errors.add("device_not_connected")
            errors.add(ERROR_DEVICE_NOT_CONNECTED)
        }
        return errors
    }

    override fun bleDevicesNeeded(): Set<String> = deviceIdentifier

    override fun ableToAutomaticallyStart(): Boolean = observerAccessible()

    override fun applyObservationConfig(settings: Map<String, Any>) {
        offlineMode = Polar360Controller.isOfflineRecordingMode(settings)
    }

    private fun hasPermissions(context: Context): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_DENIED
        }
    }

    private fun listenToDeviceConnection(): Job {
        return Scope.launch {
            BluetoothStateManagement.connectedDevices.collect { devices ->
                if (!deviceIdentifier.anyNameIn(devices)) {
                    pauseObservation(Polar360PpiType(emptySet()))
                    Polar360Controller.onDeviceDisconnected()
                }
            }
        }.second
    }
}
