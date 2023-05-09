package it.unipi.dii.masss_project

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat

class SensorGyroscope(private val sensorManager: SensorManager) : SensorEventListener {

    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Implement the onSensorChanged method to handle gyroscope sensor events
    override fun onSensorChanged(event: SensorEvent) {
        // Retrieve gyroscope data from the SensorEvent object
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Do something with the gyroscope data
        // For example, log the gyroscope data to the console
        Log.d("Gyroscope: ", "x=$x y=$y z=$z")
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

class SensorAccelerometer(private val sensorManager: SensorManager) : SensorEventListener {

    // Define variables to store the sensor manager and the gyroscope sensor
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Implement the onSensorChanged method to handle gyroscope sensor events
    override fun onSensorChanged(event: SensorEvent) {
        // Retrieve gyroscope data from the SensorEvent object
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Do something with the gyroscope data
        // For example, log the gyroscope data to the console
        Log.d("Accelerometer: ", "x=$x y=$y z=$z")
    }

    // Implement the onAccuracyChanged method to handle gyroscope sensor accuracy changes
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something when the gyroscope sensor accuracy changes
    }

    // Register the gyroscope sensor listener when the GyroscopeManager object is created
    init {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Register the gyroscope sensor listener when the GyroscopeManager object is created
    fun start() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Unregister the gyroscope sensor listener when the GyroscopeManager object is destroyed
    fun stop() {
        sensorManager.unregisterListener(this)
    }
}

class SensorMicrophone(private val context: Context, private val activity: RecordingActivity, private val sensorManager: SensorManager) {
    private var microphone: MediaRecorder? = null
    fun start(){
        if(checkPermission()){
            val savePath = Environment.getExternalStorageDirectory().toString() +"/travelAudioRecording.3gp"
            println("Here")
            microphone!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            println("Here")
            microphone!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            println("Here")
            microphone!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            println("Here")
            microphone!!.setOutputFile(savePath)
            println("Here")
            microphone!!.prepare()
            println("Here")
            microphone!!.start()
            println("Here")
        }else{
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    fun stop(){
        microphone!!.stop()
    }

    private fun checkPermission():Boolean{
        return (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }
}