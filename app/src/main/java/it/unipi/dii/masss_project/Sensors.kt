package it.unipi.dii.masss_project

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorAccelerometer : SensorEventListener {

    private val samplingInterval : Long = 1000 // 1 second
    private var lastUpdateAccelerometer: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()

            // Only process one sample per second
            if (currentTime - lastUpdateAccelerometer > this.samplingInterval) {
                lastUpdateAccelerometer = currentTime

                // Get the accelerometer values
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // TODO Do something with this values
                println("Accelerometer data : x:${x} + y:${y} + z:${z}")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO -- What should I  do when accuracy decreases/increases?
        if (sensor != null) {
            if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
                // Sensor data is highly accurate
                println("Accuracy high!")
            } else if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                // Sensor data is moderately accurate
                println("Accuracy moderately accurate!")
            } else if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                // Sensor data is less accurate
                println("Accuracy low!")

            }
        }
    }
}
