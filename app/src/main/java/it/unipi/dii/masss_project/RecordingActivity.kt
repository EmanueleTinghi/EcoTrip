package it.unipi.dii.masss_project

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding

class RecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var username: String

    private lateinit var db: FirebaseFirestore

    private var gyroscope: SensorGyroscope? = null
    private var accelerometer: SensorAccelerometer? = null
    private var microphone: SensorMicrophone? = null

    private lateinit var meansOfTransportDetected: String

    private lateinit var startCity: String
    private var startPoint: Location = Location("Start point")
    private var endPoint: Location = Location("End point")

    private val distances = mutableListOf<Float>()
    private var finalDistance = 0.0f

    private var startedRecording: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve username from intent
        username = intent.getStringExtra("username").toString()

        binding= DataBindingUtil.setContentView(
            this, R.layout.activity_recording)

        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()

        // add welcome text view
        val welcomeTextView: TextView = binding.welcomeTextView
        "Welcome ${username}!".also { welcomeTextView.text = it }
        welcomeTextView.visibility = View.VISIBLE

        // add listener for startButton
        val startButton: Button = binding.startButton
        startButton.setOnClickListener {onStartAttempt(binding, this) }

        // add listener for resultButton
        val resultButton: Button = binding.resultButton
        resultButton.setOnClickListener {onResult() }

        // add listener for logoutButton
        val logoutButton: ImageButton = binding.logoutButton
        logoutButton.setOnClickListener {onLogout() }

        // Check if the user has granted location permissions at runtime
        checkLocationPermission()

    }

    private fun onLogout() {
        val startButton: Button = binding.startButton
        if(startButton.text == "Start") {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            val message = "You hae to stop recording first"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, message, duration)
            toast.show()
        }
    }

    private fun onStartAttempt(binding: ActivityRecordingBinding, activity: RecordingActivity) {
        if (binding.startButton.text == "Start") {
            "Stop".also { binding.startButton.text = it }

            // Block the user in this activity until stopped recording
            startedRecording = true

            // hide resultButton when user starts a trip
            val resultButton: Button = binding.resultButton
            resultButton.visibility = View.INVISIBLE

            val message = "Start transportation mode detection"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, message, duration)
            toast.show()

            /**************** Get an instance of the SensorManager ****************/
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            /********************          Accelerometer          ********************/
            //Initialize the accelerometer sensor
            accelerometer = SensorAccelerometer(sensorManager)
            accelerometer!!.start()

            /********************          Gyroscope          ********************/
            //Initialize the gyroscope sensor
            gyroscope = SensorGyroscope(sensorManager)
            gyroscope!!.start()

            /********************          Microphone          ********************/
            //Initialize the gyroscope sensor
            microphone = SensorMicrophone(this, activity, sensorManager)

            //microphone!!.start()

            /****************           GPS              ****************/
            // retrieve user current location - start point
            val progress = StringBuilder()
            progress.append("Start")
            getLocation(progress)

            /****************           TO-DO              ****************/
            // todo: usare classificatore per rilevare mezzo di trasporto

        } else {
            gyroscope!!.stop()
            accelerometer!!.stop()
            //microphone!!.stop()

            // Block the user in this activity until stopped recording
            startedRecording = false

            "Start".also { binding.startButton.text = it }

            val resultButton: Button = binding.resultButton
            resultButton.visibility = View.VISIBLE

            val message = "Stop transportation mode detection"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(this, message, duration)
            toast.show()

            // todo: obtain classification results
            // meansOfTransportDetected = "car"

            // retrieve user current location - end point
            // and calculate distance between start and end points
            val progress = StringBuilder()
            progress.append("Stop")
            getLocation(progress)

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

        // get the application context
        val context = this

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

                        // get start city from gps coordinates
                        val geocoderTask = GeocoderTask(context, object : GeocoderTask.OnGeocoderCompletedListener {
                            override fun onGeocoderCompleted(cityName: String?) {
                                if (cityName != null) {
                                    startCity = cityName
                                    println("START CITY: $startCity")
                                }
                            }
                        })
                        geocoderTask.execute(startPoint)

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

                        // empty distances list
                        distances.clear()

                        // Stop receiving location updates
                        locationManager.removeUpdates(this)

                        // retrieve aggregate results for start city, if any
                        val query = db.collection("aggregateResults").whereEqualTo("city", startCity)
                        query.get().addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                // No documents found for start city -> create a new document
                                lateinit var aggregateResults: AggregateResults
                                if(finalDistance > 0 && finalDistance < 1){
                                    when(meansOfTransportDetected){
                                        "bus" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "car" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "train" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "walking" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 1),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                    }
                                } else if(finalDistance >= 1 && finalDistance < 5) {

                                    when(meansOfTransportDetected){
                                        "bus" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "car" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "train" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "walking" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 1),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                    }

                                } else if(finalDistance >= 5 && finalDistance < 10) {

                                    when(meansOfTransportDetected){
                                        "bus" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "car" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "train" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "walking" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 1),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                    }

                                } else if(finalDistance >= 10) {

                                    when(meansOfTransportDetected){
                                        "bus" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "car" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0)
                                            )
                                        )
                                        "train" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0)
                                            )
                                        )
                                        "walking" -> aggregateResults = AggregateResults(
                                            city = startCity,
                                            travelDistances = mapOf(
                                                "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                                                "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 1)
                                            )
                                        )
                                    }
                                }
                                val aggregateResultsRef = db.collection("aggregateResults").document(startCity)
                                aggregateResultsRef.set(aggregateResults)

                            } else {
                                // Document(s) found for start city -> update document
                                val increment = FieldValue.increment(1)
                                lateinit var fieldPath: String
                                if(finalDistance > 0 && finalDistance < 1){
                                    when(meansOfTransportDetected){
                                        "bus" -> fieldPath = "travelDistances.range(<1km).bus"
                                        "car" -> fieldPath = "travelDistances.range(<1km).car"
                                        "train" -> fieldPath = "travelDistances.range(<1km).train"
                                        "walking" -> fieldPath = "travelDistances.range(<1km).walking"
                                    }
                                } else if(finalDistance >= 1 && finalDistance < 5) {
                                    when(meansOfTransportDetected){
                                        "bus" -> fieldPath = "travelDistances.range(1-5km).bus"
                                        "car" -> fieldPath = "travelDistances.range(1-5km).car"
                                        "train" -> fieldPath = "travelDistances.range(1-5km).train"
                                        "walking" -> fieldPath = "travelDistances.range(1-5km).walking"
                                    }
                                } else if(finalDistance >= 5 && finalDistance < 10) {
                                    when(meansOfTransportDetected){
                                        "bus" -> fieldPath = "travelDistances.range(5-10km).bus"
                                        "car" -> fieldPath = "travelDistances.range(5-10km).car"
                                        "train" -> fieldPath = "travelDistances.range(5-10km).train"
                                        "walking" -> fieldPath = "travelDistances.range(5-10km).walking"
                                    }
                                } else if(finalDistance >= 10) {
                                    when(meansOfTransportDetected){
                                        "bus" -> fieldPath = "travelDistances.range(>10km).bus"
                                        "car" -> fieldPath = "travelDistances.range(>10km).car"
                                        "train" -> fieldPath = "travelDistances.range(>10km).train"
                                        "walking" -> fieldPath = "travelDistances.range(>10km).walking"
                                    }
                                }
                                for(document in documents) {
                                    val docRef = document.reference
                                    docRef.update(fieldPath, increment)
                                        .addOnSuccessListener { Log.d(TAG, "Incremented $fieldPath value") }
                                        .addOnFailureListener { e -> Log.w(TAG, "Error incrementing $fieldPath value", e) }

                                }
                            }
                        }.addOnFailureListener { exception ->
                            // Handle any errors here
                            val message = "${exception.message}"
                            val duration = Toast.LENGTH_LONG
                            val toast = Toast.makeText(context, message, duration)
                            toast.show()
                        }
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing to disable the button
    }

}

