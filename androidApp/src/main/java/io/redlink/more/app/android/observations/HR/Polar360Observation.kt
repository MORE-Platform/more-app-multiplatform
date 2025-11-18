package io.redlink.more.app.android.observations.HR

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarFirstTimeUseConfig
import com.polar.sdk.api.model.PolarSensorSetting
import com.polar.sdk.api.model.PolarTemperatureData
import io.github.aakira.napier.Napier
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.observations.pauseObservation
import io.redlink.more.app.android.observations.showPermissionAlertDialog
import io.redlink.more.app.android.services.sensorsListener.BluetoothStateListener
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.extensions.anyNameIn
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.Polar360Type
import io.redlink.more.more_app_mutliplatform.scopes.Scope
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothStateManagement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.collections.filter
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.let
import kotlin.run
import kotlin.stackTraceToString
import kotlin.text.contains
import kotlin.text.lowercase
import kotlin.to


private val permissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        setOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }


class Polar360Observation(repos: MainRepository):
    Observation(
        repos,
        observationType = Polar360Type(permissions)
    ) {
    data class tmpItem (
        val temp : Float,
        val timestamp: Long,
    )

    data class accItem(
        val x:Int,
        val y:Int,
        val z:Int,
        val timestamp: Long,
    )
    data class hrData(
        val hr : Int ,
        val timestamp: ULong,
        val ppiInMs : Int,
        val ppiErrorEstimate: Int
    )

    data class SyncedPacket(
        val hr: hrData,
        val temp: tmpItem,
        val acc: List<accItem>?

    )

    private val deviceManager = BluetoothStateManagement
    private val deviceIdentifier = setOf("Polar")
    private val polarConnector = MoreApplication.polarConnector!!
    private var heartRateDisposable: Disposable? = null
    private var deviceConnectionListener: Job? = null

    private var tempDisposable : Disposable? = null
    private var accDisposable : Disposable? = null
    private var hrQueue: BoundedQueue<hrData>? = BoundedQueue<hrData>(10)
    private var tmpQueue: BoundedQueue<tmpItem>? =BoundedQueue<tmpItem>(10)
    private var accQueue: BoundedQueue<List<accItem>>? = BoundedQueue<List<accItem>>(10)
    private var firstimeUseDisposable:Disposable? = null
    private val mutableStatDeviceId: MutableStateFlow<String> = MutableStateFlow("")

    private var samplingRate : Int = 1

    private  fun tryBuildPacket(): SyncedPacket? {
        if ((hrQueue?.size() != 0 ) && (tmpQueue?.size() != 0) &&  (accQueue?.size()) != 0) {
            val hr = hrQueue?.pollLast()
            val temp = tmpQueue?.pollLast()
            val acc = accQueue?.pollLast() // optional
            if (hr != null && temp != null && acc!=null) {
                return SyncedPacket(hr, temp, acc)
            }
        }
        return null
    }

    fun sendOut(packet: SyncedPacket) {
        storeData(packet)
    }

    override fun start(): Boolean {
        Napier.d(tag = "Polar360::start") { "Trying to start Polar 360 Observation..." }
        if (observerAccessible()){
            val polarDevices = deviceManager.connectedDevices.value.filter {
                (it.deviceName?.lowercase()?.contains("polar") ?: false) && (it.deviceName?.lowercase()?.contains("360") ?: false) && it.address != null
            }
            return polarDevices.firstOrNull()?.let {
                try {
                    polarConnector.polarApi.enableSDKMode(it.deviceId!!)
                    firstimeUseDisposable = checkIfDeviceIsSetup(it.deviceId!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ setupDone ->
                            if (!setupDone) {
                                Log.e(TAG, "Device setup failed, aborting start")
                                return@subscribe
                            }
                            heartRateDisposable = ppiStream(it.deviceId!!)
                            // Query available settings first
                            tempDisposable= tempstream(it.deviceId!!)
                            accDisposable =accStream(it.deviceId!!)
                            // continue starting HR/ACC/TEMP streaming here
                        }, { error ->
                            Log.e(TAG, "Setup check failed: ${error.localizedMessage}")
                        })
                    mutableStatDeviceId.set(it.deviceId!!)



                    deviceConnectionListener = listenToDeviceConnection()
                    return true

                } catch (exception: Exception) {
                    Napier.e(tag = "PolarHeartRateObservation::start") { exception.stackTraceToString() }
                    showObservationErrorNotification(
                        stringResource(R.string.observation_cannot_start),
                        stringResource(R.string.observation_error)
                    )
                    false
                }
            } ?: run {
                Napier.d(tag = "PolarHeartRateObservation::start") { "No connected devices..." }
                showObservationErrorNotification(
                    stringResource(R.string.observation_cannot_start),
                    stringResource(R.string.observation_error)
                )
                false
            }
        }
        Napier.d(tag = "PolarHeartRateObservation::start") { "No connected devices..." }
        showObservationErrorNotification(
            stringResource(R.string.observation_cannot_start),
            stringResource(R.string.observation_error)
        )
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        var streamDisposable1 : Disposable? = null
        var streamDisposable2 : Disposable? = null


        streamDisposable1 = polarConnector.polarApi.stopOfflineRecording(mutableStatDeviceId.value,PolarBleApi.PolarDeviceDataType.TEMPERATURE)
            .observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(
                {
                    Napier.d(tag = TAG, message = "Sucessfully stopped temp stream")
                },
                {
                    Napier.d(tag = TAG, message = "could not stop the tmp stream")

                }
            )
        streamDisposable2 = polarConnector.polarApi.stopOfflineRecording(mutableStatDeviceId.value,PolarBleApi.PolarDeviceDataType.ACC)
            .observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(
                {
                    Napier.d(tag = TAG, message = "Sucessfully stopped ACC stream")
                },
                {
                    Napier.d(tag = TAG, message = "could not stop the acc stream")

                }
            )
        heartRateDisposable?.dispose()
        deviceConnectionListener?.cancel()
        tempDisposable?.dispose()
        accDisposable?.dispose()
        firstimeUseDisposable?.dispose()
        deviceConnectionListener = null
        onCompletion()
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

    override fun bleDevicesNeeded(): Set<String> {
        return deviceIdentifier
    }

    override fun ableToAutomaticallyStart(): Boolean {
        return observerAccessible()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {

        val value = settings["sampling_rate"]
        if(value!= null){
            if (value is Number) {
                samplingRate = value.toInt()

            } else {
                // Fallback: convert to string and try parsing
                val stringValue = value.toString()
                val intValue = stringValue.toIntOrNull()

                if (intValue != null) {
                    samplingRate = intValue
                } else {
                    println("Warning: sampling_rate is not a valid number: $value")
                }
            }
        }

    }

    private fun hasPermissions(context: Context): Boolean {
        permissions.forEach { permission ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                Napier.e(tag = "PolarHeartRateObservation::hasPermission") { "Polar has no bluetooth permissions!" }
                return false
            }
        }
        Napier.d(tag = "PolarHeartRateObservation::hasPermission") { "Polar has Bluetooth Permission!" }
        return true
    }

    private fun listenToDeviceConnection(): Job {
        return Scope.launch {
            BluetoothStateManagement.connectedDevices.collect { devices ->
                if (!deviceIdentifier.anyNameIn(devices)) {
                    pauseObservation(Polar360Type(emptySet()))

                    Napier.d(tag = "PolarHeartRateObservation::Companion::listenToDeviceConnection") { "HR Feature removed!" }
                }
            }
        }.second

    }

    private fun checkIfDeviceIsSetup(deviceId: String): Single<Boolean> {

        polarConnector.polarApi.setAutomaticTrainingDetectionSettings(
            deviceId,
            automaticTrainingDetectionMode = true,
            automaticTrainingDetectionSensitivity = 1,
            minimumTrainingDurationSeconds = 5
        )

        return polarConnector.polarApi.isFtuDone(deviceId)
            .flatMap { ftuDone ->
                if (ftuDone) {
                    println("FTUDONE")
                    Single.just(true)
                } else {
                    println("FTU init")
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


    private fun accStream(deviceId:String) : Disposable{
        return polarConnector.polarApi.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { error: Throwable ->
                Log.e(TAG, "Settings request failed. Reason: $error")
                Single.just(
                    PolarSensorSetting(
                        hashMapOf(
                            PolarSensorSetting.SettingType.SAMPLE_RATE to 1,
                            PolarSensorSetting.SettingType.RESOLUTION to 1
                        )
                    )
                )
            }
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .toFlowable()
            .flatMap { settings: PolarSensorSetting ->
                Log.d(TAG, "Using temperature settings: $settings")
                // Explicit type to help inference
                polarConnector.polarApi.startAccStreaming(deviceId, settings) as Flowable<PolarAccelerometerData>
            }
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .doOnSubscribe {
                Log.d(TAG, "Temperature stream starting...")
            }
            .subscribe(
                { data: PolarAccelerometerData ->
                    if (data.samples.isNotEmpty()) {

                       val items : List<accItem> =  data.samples.map { sample ->
                            accItem(
                                x = sample.x,
                                y = sample.y,
                                z = sample.z,
                                timestamp = sample.timeStamp
                            )
                        }
                        if (accQueue!!.size() == 0){
                        accQueue!!.add(items)}
                        else{
                            val from_queue : MutableList<accItem> = accQueue!!.pollLast() as MutableList<accItem>
                            from_queue.addAll(items)
                            accQueue!!.add(from_queue)
                        }
                    }
                },
                { error ->
                    Log.e(TAG, "Temperature stream failed: ${error.localizedMessage}")

                    accDisposable=null
                }
            )
    }
    private fun tempstream(deviceId: String): Disposable{
        return polarConnector.polarApi.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.TEMPERATURE)
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { error: Throwable ->
                Log.e(TAG, "Settings request failed. Reason: $error")
                Single.just(
                    PolarSensorSetting(
                        hashMapOf(
                            PolarSensorSetting.SettingType.SAMPLE_RATE to 1,
                            PolarSensorSetting.SettingType.RESOLUTION to 1
                        )
                    )
                )
            }
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .toFlowable()
            .flatMap { settings: PolarSensorSetting ->
                Log.d(TAG, "Using temperature settings: $settings")
                // Explicit type to help inference
                polarConnector.polarApi.startTemperatureStreaming(deviceId, settings) as Flowable<PolarTemperatureData>
            }
            .throttleFirst(samplingRate.toLong(), TimeUnit.SECONDS, Schedulers.io())
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .doOnSubscribe {
                Log.d(TAG, "Temperature stream starting...")
            }
            .subscribe(
                { data: PolarTemperatureData ->
                    if (data.samples.isNotEmpty()) {
                        val sample = data.samples[0]
                        val temp = sample.temperature
                        Log.d(TAG, "Temperature: $temp °C, timestamp: ${sample.timeStamp}")
                        tmpQueue!!.add(tmpItem(sample.temperature,sample.timeStamp))
                        tryBuildPacket()?.let { sendOut(it) }
                    }
                },
                { error ->
                    Log.e(TAG, "Temperature stream failed: ${error.localizedMessage}")

                    tempDisposable=null
                }
            )
    }

    private fun ppiStream(deviceId:String): Disposable{
        return polarConnector.polarApi.startPpiStreaming(deviceId)
            .throttleFirst(samplingRate.toLong(), TimeUnit.SECONDS, Schedulers.io())
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(
            { polarData ->
                Log.d(TAG, "HR: ${polarData.samples[0].hr} ")
                hrQueue!!.add(hrData(polarData.samples[0].hr,polarData.samples[0].timeStamp,polarData.samples[0].ppi,polarData.samples[0].errorEstimate))
                tryBuildPacket()?.let { println(it) }
            },
            { error ->
                Napier.e(
                    tag = "PolarHeartRateObservation::start",
                    message = "HR Recording error: ${error.stackTraceToString()}"
                )
                pauseObservation(Polar360Type(emptySet()))
                showObservationErrorNotification(
                    stringResource(R.string.observation_bluetooth_error),
                    stringResource(R.string.observation_error)
                )
            })
    }


}