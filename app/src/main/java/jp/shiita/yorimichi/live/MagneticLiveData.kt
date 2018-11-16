package jp.shiita.yorimichi.live

import android.arch.lifecycle.LiveData
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class MagneticLiveData(context: Context) : LiveData<Float>() {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerReading = FloatArray(16)
    private val magnetometerReading = FloatArray(16)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var accelerometerReady: Boolean = false
    private var magneticReady: Boolean = false

    private val listener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return

            when (event.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    lowPassFilter(accelerometerReading, event.values.clone())
                    accelerometerReady = true
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    lowPassFilter(magnetometerReading, event.values.clone())
                    magneticReady = true
                }
                else -> return
            }

            if (accelerometerReady && magneticReady) {
                magneticReady = false
                SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        accelerometerReading,
                        magnetometerReading
                )
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val angle = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                value = angle + if (angle < 0) 360 else 0
            }
        }
    }

    override fun onActive() {
        manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
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