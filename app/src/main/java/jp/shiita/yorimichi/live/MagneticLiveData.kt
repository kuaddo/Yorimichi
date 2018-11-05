package jp.shiita.yorimichi.live

import android.arch.lifecycle.LiveData
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class MagneticLiveData(context: Context) : LiveData<Float>() {
    val manager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    var accelerometerValues = FloatArray(3)
    var geomagneticMatrix = FloatArray(3)
    var accelerometerReady: Boolean = false
    var magneticReady: Boolean = false

    private val listener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            when (event.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> {
//                    lowPassFilter(accelerometerValues, event.values.clone())
                    accelerometerValues = event.values.clone()
                    accelerometerReady = true
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
//                    lowPassFilter(geomagneticMatrix, event.values.clone())
                    geomagneticMatrix = event.values.clone()
                    magneticReady = true
                }
                else -> return
            }

            if (accelerometerReady && magneticReady) {
                magneticReady = false

                val r = FloatArray(16)
                val i = FloatArray(16)
                val rR = FloatArray(16)
                val actualOrientation = FloatArray(3)

                SensorManager.getRotationMatrix(r, i, accelerometerValues, geomagneticMatrix)
                SensorManager.remapCoordinateSystem(r, SensorManager.AXIS_X, SensorManager.AXIS_Z, rR)
                SensorManager.getOrientation(rR, actualOrientation)

                value = Math.toDegrees(actualOrientation[0].toDouble()).toFloat()
            }
        }
    }

    override fun onActive() {
        manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI)
        manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  SensorManager.SENSOR_DELAY_UI)
    }

    override fun onInactive() {
        manager.unregisterListener(listener)
    }

    private fun lowPassFilter(vecPrev: FloatArray, vecNew: FloatArray) {
        for (i in vecNew.indices) {
            vecPrev[i] = ALPHA * vecPrev[i] + (1 - ALPHA) * vecNew[i]
        }
    }

    companion object {
        const val ALPHA = 0.8f
    }
}