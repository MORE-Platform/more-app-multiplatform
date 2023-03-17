package io.redlink.more.more_app_mutliplatform.android.observations

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.ObservationTypeImpl
import io.redlink.more.more_app_mutliplatform.observations.ObservationTypes.AccelerometerType
import org.json.JSONObject

private const val TAG = "AccelerometerObservation"

class AccelerometerObservation(
    context: Context,
    sensorPermissions: Set<String> = emptySet()
) : Observation(observationTypeImpl = AccelerometerType(sensorPermissions)), SensorEventListener {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var sampleFrequency: Int = SensorManager.SENSOR_DELAY_NORMAL


    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let {
            if (it.isNotEmpty()) {
                storeData(mapOf("x" to it[0], "y" to it[1], "z" to it[2]))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Sensor accuracy changed to $accuracy")
    }

    override fun start(): Boolean {
        return sensor?.let {
            running = sensorManager.registerListener(this, it, sampleFrequency)
            running
        } ?: false
    }

    override fun stop() {
        sensor?.let {
            sensorManager.unregisterListener(this)
            running = false
        }
    }

    override fun observerAccessible(): Boolean {
        return this.sensor != null
    }
}