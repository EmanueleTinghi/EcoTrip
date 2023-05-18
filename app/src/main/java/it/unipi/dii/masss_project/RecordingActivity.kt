package it.unipi.dii.masss_project

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding

class RecordingActivity : AppCompatActivity() {

    private lateinit var sensorsCollector: SensorsCollector

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var util: Util

    private lateinit var username: String

    private var gyroscope: SensorGyroscope? = null
    private var accelerometer: SensorAccelerometer? = null
    private var magneticField: SensorMagneticField? = null

    private lateinit var authManager : FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager

    private lateinit var meansOfTransportDetected: String

    private lateinit var gpsHandler : GPSHandler

    private var finalDistance: Double = 0.0

    private var startedRecording: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorsCollector = SensorsCollector(applicationContext)

        // to keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // retrieve username from intent
        username = intent.getStringExtra("username").toString()

        binding= DataBindingUtil.setContentView(
            this, R.layout.activity_recording)

        util = Util(this, null)

        // initialize firebase firestore manager
        firestoreManager = FirestoreManager()
        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(this, null, firestoreManager)

        // initialize GPSHandler
        gpsHandler = GPSHandler(this)

        // add welcome text view
        val welcomeTextView: TextView = binding.welcomeTextView
        "Welcome ${username}!".also { welcomeTextView.text = it }
        welcomeTextView.visibility = View.VISIBLE

        // add listener for startButton
        val startButton: Button = binding.startButton
        startButton.setOnClickListener {onStartAttempt() }

        // add listener for resultButton
        val resultButton: Button = binding.resultButton
        resultButton.setOnClickListener {onResult() }

        // add listener for logoutButton
        val logoutButton: ImageButton = binding.logoutButton
        logoutButton.setOnClickListener {onLogout() }

        // Check if the user has granted location permissions at runtime
        gpsHandler.checkLocationPermission()

    }

    private fun onLogout() {
        val startButton: Button = binding.startButton
        if(startButton.text == "Start") {
            authManager.logoutUser()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            util.showToast("You have to stop recording first")
        }
    }

    private fun onStartAttempt() {
        if (binding.startButton.text == "Start") {
            "Stop".also { binding.startButton.text = it }

            // Block the user in this activity until stopped recording
            startedRecording = true

            // hide resultButton when user starts a trip
            val resultButton: Button = binding.resultButton
            resultButton.visibility = View.INVISIBLE

            util.showToast("Start transportation mode detection")

            /**************** Get an instance of the SensorManager ****************/
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            /********************          Accelerometer          ********************/
            //Initialize the accelerometer sensor
            accelerometer = SensorAccelerometer(sensorManager, sensorsCollector)
            accelerometer!!.start()

            /********************          MagneticField          ********************/
            //Initialize the magneticField sensor
            magneticField = SensorMagneticField(sensorManager, sensorsCollector)
            magneticField!!.start()

            /********************          Gyroscope          ********************/
            //Initialize the gyroscope sensor
            gyroscope = SensorGyroscope(sensorManager, sensorsCollector)
            gyroscope!!.start()

            /****************           GPS              ****************/
            // retrieve user current location - start point
            gpsHandler.progress = "Start"
            gpsHandler.finalDistance = 0.0
            getLocation()

            /****************           Starting collection sampling              ****************/
            sensorsCollector.startCollection()

        } else {
            gpsHandler.progress = "Stop"

            gyroscope!!.stop()
            accelerometer!!.stop()
            magneticField!!.stop()

            meansOfTransportDetected = sensorsCollector.stopCollection()
            println("class label $meansOfTransportDetected")

            // Block the user in this activity until stopped recording
            startedRecording = false

            "Start".also { binding.startButton.text = it }

            val resultButton: Button = binding.resultButton
            resultButton.visibility = View.VISIBLE

            util.showToast("Stop transportation mode detection")

            // retrieve user current location - end point
            // and calculate distance between start and end points
            getLocation()
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

    private fun getLocation() {

        if (gpsHandler.progress == "Start") {
            // Register the listener to receive location updates
            gpsHandler.startReceivingUpdates()

        } else if (gpsHandler.progress == "Stop") {

            // calculate final distance
            finalDistance = gpsHandler.finalDistance
            if (finalDistance < 0.0)
                finalDistance = 0.0
            println("FINAL DISTANCE: $finalDistance km")

            gpsHandler.stopReceivingUpdates()

            firestoreManager.updateStorage(authManager.getUserID(), meansOfTransportDetected, gpsHandler.startCity, finalDistance, util)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing to disable the button
    }

}

