package io.redlink.more.more_app_mutliplatform.android.observations

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.AccelerometerType

private const val TAG = "AccelerometerObservation"
class AccelerometerObservation(
    context: Context
) : Observation(observationType = AccelerometerType(emptySet())), SensorEventListener {
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
            sensorManager.registerListener(this, it, sampleFrequency)
        } ?: false
    }

    override fun stop() {
        sensor?.let {
            sensorManager.unregisterListener(this)
        }
    }

    override fun observerAccessible(): Boolean {
        return this.sensor != null
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
    }
}