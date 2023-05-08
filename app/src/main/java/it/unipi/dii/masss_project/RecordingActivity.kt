package it.unipi.dii.masss_project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var username: String

    private var lastUpdateAccelerometer: Long = 0

    private var startPoint: Location = Location("Start point")
    private var endPoint: Location = Location("End point")

    private val distances = mutableListOf<Float>()
    private var finalDistance = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve username from intent
        username = intent.getStringExtra("username").toString()

        binding= DataBindingUtil.setContentView(
            this, R.layout.activity_recording)

        // add welcome text view
        val welcomeTextView: TextView = binding.welcomeTextView
        "Welcome ${username}!".also { welcomeTextView.text = it }
        welcomeTextView.visibility = View.VISIBLE

        // add listener for startButton
        val startButton: Button = binding.startButton
        startButton.setOnClickListener {onStartAttempt(binding) }

        // add listener for resultButton
        val resultButton: Button = binding.resultButton
        resultButton.setOnClickListener {onResult() }

        // Check if the user has granted location permissions at runtime
        checkLocationPermission()

    }

    private fun onStartAttempt(binding: ActivityRecordingBinding) {
        if (binding.startButton.text == "Start") {
            "Stop".also { binding.startButton.text = it }

            // hide resultButton when user starts a trip
            val resultButton: Button = binding.resultButton
            resultButton.visibility = View.INVISIBLE

            val message = "Start transportation mode detection"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, message, duration)
            toast.show()

            /**************** Get an instance of the SensorManager ****************/
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            /****************           Accelerometer              ****************/
            // Get the accelerometer sensor
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val accelerometerListener = SensorAccelerometer()
            sensorManager.registerListener(accelerometerListener,
                                                accelerometer,
                                                SensorManager.SENSOR_DELAY_NORMAL)

            /****************           GPS              ****************/
            // retrieve user current location - start point
            val progress = StringBuilder()
            progress.append("Start")
            getLocation(progress)

            /****************           TO-DO              ****************/
            // todo: usare classificatore per rilevare mezzo di trasporto

        } else {
            "Start".also { binding.startButton.text = it }

            val resultButton: Button = binding.resultButton
            resultButton.visibility = View.VISIBLE

            val message = "Stop transportation mode detection"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, message, duration)
            toast.show()

            // retrieve user current location - end point
            // and calculate distance between start and end points
            val progress = StringBuilder()
            progress.append("Stop")
            getLocation(progress)

            // todo: collezionare risultati del transportation mode


            // todo: passare ad unl'altra pagina che mostra i risultati ottenuti
            //val intent = Intent(this, ResultActivity::class.java)
            //startActivity(intent)
        }

    }

    private fun onResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    override fun onResume(){
        super.onResume()

        // retrieve username from intent
        username = intent.getStringExtra("username").toString()
        val welcomeTextView: TextView = binding.welcomeTextView
        "Welcome ${username}!".also { welcomeTextView.text = it }
        welcomeTextView.visibility = View.VISIBLE
    }

    private fun getLocation(progress: StringBuilder) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onLocationChanged(location: Location) {

                // get coordinates
                val latitude = location.latitude
                val longitude = location.longitude

                when (progress.toString()) {
                    "Start" -> {
                        // start point
                        startPoint.latitude = latitude
                        startPoint.longitude = longitude
                        println("START LOCATION: latitude ${startPoint.latitude}, longitude ${startPoint.longitude}")
                        progress.append("Intermediate")

                    }
                    "Stop" -> {

                        // stop point
                        endPoint.latitude = latitude
                        endPoint.longitude = longitude
                        println("STOP LOCATION: latitude ${endPoint.latitude}, longitude ${endPoint.longitude}")

                        // calculate distance between intermediate and end point
                        val distance = (startPoint.distanceTo(endPoint) / 1000.0).toFloat()
                        println("DISTANCE: $distance km")
                        distances.add(distance)

                        // calculate final distance
                        finalDistance = distances.sum()
                        println("FINAL DISTANCE: $finalDistance km")

                        // Stop receiving location updates
                        locationManager.removeUpdates(this)

                    }
                    else -> {
                        // intermediate point
                        endPoint.latitude = latitude
                        endPoint.longitude = longitude
                        println("INTERMEDIATE LOCATION: latitude ${endPoint.latitude}, longitude ${endPoint.longitude}")

                        // calculate distance between start and intermediate point
                        val distance = (startPoint.distanceTo(endPoint) / 1000.0).toFloat()
                        println("DISTANCE: $distance km")
                        distances.add(distance)

                        // set new start point
                        startPoint.latitude = latitude
                        startPoint.longitude = longitude
                    }
                }
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

        }
        checkLocationPermission()

        // Register the listener to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5.0f, locationListener)
    }

    private fun checkLocationPermission() {
        // Check if the user has granted location permissions at runtime
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }
    }

}