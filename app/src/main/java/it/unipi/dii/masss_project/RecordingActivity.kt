package it.unipi.dii.masss_project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding

class RecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var username: String

    private var lastUpdateAccelerometer: Long = 0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 99
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient


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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            // Check if the user has granted location permissions at runtime
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request location permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
            else {
                // Get the last location
                getLastLocation()
            }

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
        // todo : ritrovare lo username dell'utente
        username = intent.getStringExtra("username").toString()
        val welcomeTextView: TextView = binding.welcomeTextView
        "Welcome ${username}!".also { welcomeTextView.text = it }
        welcomeTextView.visibility = View.VISIBLE
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location!= null) {
                    // use the user current location
                    val latitude = location.latitude
                    val longitude = location.longitude

                    val message = "Latitude: $latitude, Longitude: $longitude"
                    val duration = Toast.LENGTH_LONG
                    val toast = Toast.makeText(this, message, duration)
                    toast.show()

                } else {
                    // location is null
                    val message = "Unable to retrieve location"
                    val duration = Toast.LENGTH_LONG
                    val toast = Toast.makeText(this, message, duration)
                    toast.show()
                }
            }
    }

}