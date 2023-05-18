package it.unipi.dii.masss_project.sensors_manager


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.pow
import kotlin.math.sqrt

class SensorGyroscope(private val sensorManager: SensorManager,
                      private val sensorsCollector: SensorsCollector
) : SensorEventListener {

    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Implement the onSensorChanged method to handle gyroscope sensor events
    override fun onSensorChanged(event: SensorEvent) {
        // Retrieve gyroscope data from the SensorEvent object
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(((x.pow(2)) + (y.pow(2)) + (z.pow(2))).toDouble())
        sensorsCollector.storeGyroscopeSample(magnitude)

    }

    // Implement the onAccuracyChanged method to handle gyroscope sensor accuracy changes
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something when the gyroscope sensor accuracy changes
    }

    // Register the gyroscope sensor listener when the GyroscopeManager object is created
    init {
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Register the gyroscope sensor listener when the GyroscopeManager object is created
    fun start() {
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Unregister the gyroscope sensor listener when the GyroscopeManager object is destroyed
    fun stop() {
        sensorManager.unregisterListener(this)
    }
}

class SensorAccelerometer(private val sensorManager: SensorManager,
                          private val sensorsCollector: SensorsCollector
) : SensorEventListener {

    // Define variables to store the sensor manager and the accelerometer sensor
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Implement the onSensorChanged method to handle accelerometer sensor events
    override fun onSensorChanged(event: SensorEvent) {
        // Retrieve accelerometer data from the SensorEvent object
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(((x.pow(2)) + (y.pow(2)) + (z.pow(2))).toDouble())
        sensorsCollector.storeAcceleratorSample(magnitude)
    }

    // Implement the onAccuracyChanged method to handle accelerometer sensor accuracy changes
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something when the accelerometer sensor accuracy changes
    }

    // Register the accelerometer sensor listener when the AccelerometerManager object is created
    init {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Register the accelerometer sensor listener when the AccelerometerManager object is created
    fun start() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Unregister the accelerometer sensor listener when the AccelerometerManager object is destroyed
    fun stop() {
        sensorManager.unregisterListener(this)
    }
}

class SensorMagneticField(private val sensorManager: SensorManager,
                         private val sensorsCollector: SensorsCollector
) : SensorEventListener {

    // Define variables to store the sensor manager and the magneticField sensor
    private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Implement the onSensorChanged method to handle magneticField sensor events
    override fun onSensorChanged(event: SensorEvent) {
        // Retrieve magneticField data from the SensorEvent object
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(((x.pow(2)) + (y.pow(2)) + (z.pow(2))).toDouble())
        sensorsCollector.storeMagneticFieldSample(magnitude)
    }

    // Implement the onAccuracyChanged method to handle magneticField sensor accuracy changes
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something when the accelerometer sensor accuracy changes
    }

    // Register the magneticField sensor listener when the MagneticFieldManager object is created
    init {
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Register the magneticField sensor listener when the MagneticFieldManager object is created
    fun start() {
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Unregister the magneticField sensor listener when the MagneticFieldManager object is destroyed
    fun stop() {
        sensorManager.unregisterListener(this)
    }
}
