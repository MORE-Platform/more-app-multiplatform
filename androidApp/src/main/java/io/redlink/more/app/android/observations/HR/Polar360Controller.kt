package io.redlink.more.app.android.observations.HR

import android.content.ContentValues.TAG
import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarFirstTimeUseConfig
import com.polar.sdk.api.model.PolarSensorSetting
import com.polar.sdk.api.model.PolarOfflineRecordingData
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.database.entities.BluetoothDeviceEntity
import io.redlink.more.services.bluetooth.BluetoothStateManagement
import org.koin.core.component.getScopeName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Polar360Controller {
    const val CONFIG_OFFLINE_RECORDING = "Offline_recording"

    interface OfflineRecordingItem {
        val timestamp: Long
        fun toMap(): Map<String, Any>
    }

    data class accItem(
        val x: Int,
        val y: Int,
        val z: Int,
        override val timestamp: Long,
    ) : OfflineRecordingItem {
        override fun toMap() = mapOf("x" to x, "y" to y, "z" to z)
    }

    data class ppi_data(
        val hr: Int,
        override val timestamp: Long,
        val ppiInMs: Int,
        val ppiErrorEstimate: Int
    ) : OfflineRecordingItem {
        override fun toMap() = mapOf("hr" to hr, "ppiInMs" to ppiInMs, "ppiErrorEstimate" to ppiErrorEstimate)
    }

    data class tmpItem(
        val temp: Float,
        override val timestamp: Long,
    ) : OfflineRecordingItem {
        override fun toMap() = mapOf("temperature" to temp)
    }

    data class hr_data(
        val hr: Int,
        override val timestamp: Long
    ) : OfflineRecordingItem {
        override fun toMap() = mapOf("hr" to hr)
    }

    private val polarConnector get() = MoreApplication.polarConnector!!
    private val bleManager = BluetoothStateManagement
    private var ftuDisposable: Disposable? = null
    private var sdkModeEnabled = false
    private var currentDeviceId: String? = null




    fun findPolar360Device(): BluetoothDeviceEntity? {
        return bleManager.connectedDevices.value.firstOrNull {
            val name = it.deviceName?.lowercase() ?: return@firstOrNull false
            name.contains("polar") && name.contains("360") && it.address != null
        }
    }

    fun ensureReady(deviceId: String, offlineMode: Boolean, onReady: () -> Unit, onError: (Throwable) -> Unit) {
        currentDeviceId = deviceId
        ftuDisposable?.dispose()
        ftuDisposable = checkIfDeviceIsSetup(deviceId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ setupDone ->
                if (setupDone) {
                    if (offlineMode) {
                        disableSdkMode(deviceId)
                    } else {
                        enableSdkMode(deviceId)
                    }
                    onReady()
                } else {
                    Log.e(TAG, "Polar 360 FTU failed")
                    onError(RuntimeException("First time use setup failed"))
                }
            }, { error ->
                Log.e(TAG, "Polar 360 setup check failed: ${error.localizedMessage}")
                onError(error)
            })
    }

    fun startOfflineRecording(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        settings: PolarSensorSetting? = null
    ): Disposable {
        return polarConnector.polarApi.disableSDKMode(deviceId)
            .onErrorComplete()
            .doOnComplete { Napier.d(tag = "Polar360Controller") { "SDK mode disabled for $dataType" } }
            .andThen(polarConnector.polarApi.stopOfflineRecording(deviceId, dataType).onErrorComplete())
            .andThen(resolveAndStartOfflineRecording(deviceId, dataType, settings))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(
                { Napier.d(tag = "Polar360Controller") { "Started offline recording for $dataType" } },
                { error -> Napier.e(tag = "Polar360Controller") { "Failed to start offline recording for $dataType: ${error.message}" } }
            )
    }

    private fun resolveAndStartOfflineRecording(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        settings: PolarSensorSetting?
    ): Completable {
        if (dataType == PolarBleApi.PolarDeviceDataType.HR || dataType == PolarBleApi.PolarDeviceDataType.PPI) {
            return polarConnector.polarApi.startOfflineRecording(deviceId, dataType)
        }
        if (settings != null) {
            return polarConnector.polarApi.startOfflineRecording(deviceId, dataType, settings, null)
        }
        return polarConnector.polarApi.requestOfflineRecordingSettings(deviceId, dataType)
            .onErrorResumeNext { error: Throwable ->
                Napier.e(tag = "Polar360Controller::$dataType") { "Settings request failed: $error" }
                Single.just(
                    PolarSensorSetting(
                        hashMapOf(
                            PolarSensorSetting.SettingType.SAMPLE_RATE to 1,
                            PolarSensorSetting.SettingType.RESOLUTION to 1
                        )
                    )
                )
            }
            .flatMapCompletable { resolvedSettings ->
                Napier.d(tag = "Polar360Controller::$dataType") { "Using settings: ${resolvedSettings.settings}" }
                polarConnector.polarApi.startOfflineRecording(deviceId, dataType, resolvedSettings, null)
            }
    }

    fun stopOfflineRecording(dataType: PolarBleApi.PolarDeviceDataType): Single<List<Any>> {
        val deviceId = currentDeviceId ?: return Single.just(emptyList())

        return polarConnector.polarApi.stopOfflineRecording(deviceId, dataType)
            .onErrorComplete()
            .andThen(
                polarConnector.polarApi.listOfflineRecordings(deviceId)
                    .doOnNext { entry ->
                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") {
                            "Found recording entry: type=${entry.type}, date=${entry.date}, size=${entry.size}"
                        }
                    }
                    .filter { entry -> entry.type == dataType }
                    .concatMap { entry ->
                        polarConnector.polarApi.getOfflineRecord(deviceId, entry, null)
                            .flatMap { data ->
                                val samples: List<Any> = when (data) {
                                    is PolarOfflineRecordingData.PpiOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "PPI samples: ${it.size}" }
                                    }
                                    is PolarOfflineRecordingData.AccOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "ACC samples: ${it.size}" }
                                    }
                                    is PolarOfflineRecordingData.TemperatureOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "TEMP samples: ${it.size}" }
                                    }
                                    is PolarOfflineRecordingData.PpgOfflineRecording -> data.data.samples.also {
                                        Napier.d(tag = "Polar360Controller::stopOfflineRecording") { "PPG samples: ${it.size}" }
                                    }
                                    else -> emptyList<Any>().also {
                                        Napier.e(tag = "Polar360Controller::stopOfflineRecording") { "Unsupported data type: ${data::class.simpleName}" }
                                    }
                                }
                                polarConnector.polarApi.removeOfflineRecord(deviceId, entry)
                                    .andThen(Single.just(samples))
                            }
                            .toFlowable()
                    }
                    .toList()
                    .map { it.flatten() }
            )
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    fun isOfflineRecordingMode(config: Map<String, Any>): Boolean {
        Napier.d(tag =  "Polar360Controller:isOfflineRecordingMode"){config.toString()}
        return config[CONFIG_OFFLINE_RECORDING] == true
                || config[CONFIG_OFFLINE_RECORDING]?.toString()?.lowercase() == "true"
    }

    fun getCurrentDeviceId(): String? = currentDeviceId

    private fun enableSdkMode(deviceId: String) {
        if (!sdkModeEnabled) {
            try {
                polarConnector.polarApi.enableSDKMode(deviceId)
                sdkModeEnabled = true
                Napier.d(tag = "Polar360Controller") { "SDK mode enabled for $deviceId" }
            } catch (e: Exception) {
                Napier.e(tag = "Polar360Controller") { "Failed to enable SDK mode: ${e.message}" }
            }
        }
    }

    private fun disableSdkMode(deviceId: String) {
        if (sdkModeEnabled) {
            try {
                polarConnector.polarApi.disableSDKMode(deviceId)
                sdkModeEnabled = false
                Napier.d(tag = "Polar360Controller") { "SDK mode disabled for $deviceId" }
            } catch (e: Exception) {
                Napier.e(tag = "Polar360Controller") { "Failed to disable SDK mode: ${e.message}" }
            }
        }
    }

    fun onDeviceDisconnected() {
        sdkModeEnabled = false
        currentDeviceId = null
    }

    fun getPolarApi() = polarConnector.polarApi

    private fun checkIfDeviceIsSetup(deviceId: String): Single<Boolean> {
        return polarConnector.polarApi.isFtuDone(deviceId)
            .flatMap { ftuDone ->
                if (ftuDone) {
                    Single.just(true)
                } else {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val deviceTime = sdf.format(calendar.time)

                    val birthCalendar = Calendar.getInstance()
                    birthCalendar.add(Calendar.YEAR, -30)

                    val ftuConfig = PolarFirstTimeUseConfig(
                        gender = PolarFirstTimeUseConfig.Gender.MALE,
                        birthDate = birthCalendar.time,
                        height = 170.0f,
                        weight = 70.0f,
                        maxHeartRate = 190,
                        vo2Max = 45,
                        restingHeartRate = 60,
                        trainingBackground = 30,
                        deviceTime = deviceTime,
                        typicalDay = PolarFirstTimeUseConfig.TypicalDay.MOSTLY_SITTING,
                        sleepGoalMinutes = 480
                    )

                    polarConnector.polarApi.doFirstTimeUse(deviceId, ftuConfig)
                        .toSingleDefault(true)
                        .onErrorReturnItem(false)
                }
            }
    }
}
