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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var username: String

    private var lastUpdateAccelerometer: Long = 0

    private var startLat: Double = 0.0
    private var startLong: Double = 0.0
    private var stopLat: Double = 0.0
    private var stopLong: Double = 0.0


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

        // register listener for startButton
        val startButton: Button = binding.startButton
        startButton.setOnClickListener {onStartAttempt(binding) }

        // register listener for resultButton
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
            getStartLocation()

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
            getStopLocation()

            // calculate distance between start and end points
            val distance = calculateDistance()
            println("distance: $distance")


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

    private fun getStartLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude

                println("start")
                startLat = latitude
                startLong = longitude
                println("start location: latitude $startLat, longitude $startLong")

                // Stop receiving location updates
                locationManager.removeUpdates(this)
            }
            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }
        checkLocationPermission()

        // Register the listener to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0f, locationListener)
        // Stop receiving location updates
        locationManager.removeUpdates(locationListener)
    }

    private fun getStopLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude

                println("stop")
                stopLat = latitude
                stopLong = longitude
                println("stop location: latitude $stopLat, longitude $stopLong")

                // Stop receiving location updates
                locationManager.removeUpdates(this)
            }
            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }
        checkLocationPermission()

        // Register the listener to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0f, locationListener)
        // Stop receiving location updates
        locationManager.removeUpdates(locationListener)
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

    private fun calculateDistance(): Double {
        println("calculateDistance")
        println("start location: latitude $startLat, longitude $startLong")
        println("stop location: latitude $stopLat, longitude $stopLong")

        val earthRadius = 6371 // Radius of the earth in km

        val dLat = Math.toRadians(stopLat - startLat)
        val dLng = Math.toRadians(stopLong - startLong)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(startLat)) * cos(Math.toRadians(stopLat)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Distance in km
    }

}