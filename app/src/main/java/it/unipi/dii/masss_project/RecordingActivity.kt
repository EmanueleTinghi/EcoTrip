package it.unipi.dii.masss_project

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding

class RecordingActivity : AppCompatActivity() {

    private lateinit var sensorsCollector: SensorsCollector

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var util: Util

    private lateinit var username: String

    private var gyroscope: SensorGyroscope? = null
    private var accelerometer: SensorAccelerometer? = null
    private var magneticField: SensorMagneticField? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var meansOfTransportDetected: String

    private lateinit var gpsHandler : GPSHandler

    private var finalDistance: Double = 0.0

    private var startedRecording: Boolean = false

    private val carScore = 1
    private val busScore = 5
    private val walkingScore = 10
    private val trainScore = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorsCollector = SensorsCollector(applicationContext)
        // to keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // retrieve username from intent
        username = intent.getStringExtra("username").toString()

        binding= DataBindingUtil.setContentView(
            this, R.layout.activity_recording)

        util = Util(this, null, binding)

        // initialize firebase authentication
        auth = FirebaseAuth.getInstance()
        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()

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
            FirebaseAuth.getInstance().signOut()
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
            finalDistance = if ( gpsHandler.distances.sum() < 0 )
                0.0
            else
                gpsHandler.distances.sum()
            println("FINAL DISTANCE: $finalDistance km")

            // empty distances list
            gpsHandler.distances.clear()

            gpsHandler.stopReceivingUpdates()

            updateStorage()
        }
    }

    private fun updateStorage() {

        // retrieve and update aggregate results for start city, if any
        val query = db.collection("aggregateResults").whereEqualTo("city", gpsHandler.startCity)
        query.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // No documents found for start city -> create a new document
                val aggregateResults = initializeAggregateResults()
                val aggregateResultsRef = db.collection("aggregateResults").document(gpsHandler.startCity)
                aggregateResultsRef.set(aggregateResults)

            } else {
                if (meansOfTransportDetected != "still") {
                    // Document(s) found for start city -> update document
                    val increment = FieldValue.increment(1)
                    val fieldPath = initializeFieldPath()
                    for (document in documents) {
                        val docRef = document.reference
                        docRef.update(fieldPath, increment)
                            .addOnSuccessListener {
                                Log.d(
                                    TAG,
                                    "Incremented $fieldPath value of aggregateResults collection"
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    TAG,
                                    "Error incrementing $fieldPath value of aggregateResults collection",
                                    e
                                )
                            }

                    }
                }
            }
        }.addOnFailureListener { exception ->
            exception.message?.let { util.showToast(it) }
        }

        // Get the currently signed-in user
        val currentUser = auth.currentUser
        // Retrieve the user ID
        val userID = currentUser?.uid
        if (userID != null) {
            // retrieve user document to update its aggregate results
            val userRef = db.collection("users").document(userID)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.data?.containsKey("results") == true) {
                        // the user document contains aggregate results
                        val results = document.data?.get("results") as HashMap<*, *>

                        // get aggregate user results for start city, if any
                        if (results.containsKey(gpsHandler.startCity)) {
                            if (meansOfTransportDetected != "still") {
                                // user have aggregate results for start city -> update aggregate results
                                val increment = FieldValue.increment(1)
                                val fieldPath = "results.$gpsHandler.startCity." + initializeFieldPath()
                                val docRef = document.reference
                                docRef.update(fieldPath, increment)
                                    .addOnSuccessListener {
                                        Log.d(
                                            TAG,
                                            "Incremented $fieldPath value of user collection"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            TAG,
                                            "Error incrementing $fieldPath value of user collection",
                                            e
                                        )
                                    }
                            }
                        } else {
                            // user have no aggregate results for start city -> insert new aggregate results in results map
                            val aggregateResults = initializeAggregateResults()
                            val docRef = document.reference
                            docRef.update("results.$gpsHandler.startCity", aggregateResults)
                                .addOnSuccessListener { Log.d(TAG, "Added new aggregate results for new city in user collection") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error adding new aggregate results for new city in user collection", e) }
                        }
                    } else {
                        // the user haven't any aggregate results yet -> create results field
                        val aggregateResults = initializeAggregateResults()
                        val results = hashMapOf(
                            gpsHandler.startCity to aggregateResults
                        )
                        val docRef = document.reference
                        docRef.update("results",results)
                            .addOnSuccessListener { Log.d(TAG, "Results added to user document") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error adding results to user document", e) }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    exception.message?.let { util.showToast(it) }
                }
        }

        // calculate green score
        var generalWeightedAverage = 0.0
        var userWeightedAverage = 0.0

        val query2 = db.collection("aggregateResults").whereEqualTo("city", gpsHandler.startCity)
        query2.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                for(document in documents) {
                    val range = getRange()
                    val travelDistance = document.get("travelDistances.$range") as HashMap<*, *>
                    val totBus = travelDistance["bus"] as Long
                    val totTrain = travelDistance["train"] as Long
                    val totWalking = travelDistance["walking"] as Long
                    val totCar = travelDistance["car"] as Long
                    if ((totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble()) != 0.0) {
                        generalWeightedAverage = (totBus.toDouble() * busScore + totTrain.toDouble() * trainScore + totWalking.toDouble() * walkingScore + totCar.toDouble() * carScore) /
                                (totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble())
                    }
                    println("General Weighted Average: $generalWeightedAverage")
                }
            }
        }.addOnFailureListener { exception ->
            exception.message?.let { util.showToast(it) }
        }

        if (userID != null) {
            val userRef = db.collection("users").document(userID)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.data?.containsKey("results") == true) {
                        val results = document.data?.get("results") as HashMap<*, *>
                        if (results.containsKey(gpsHandler.startCity)) {
                            val range = getRange()
                            val aggregateResults = results[gpsHandler.startCity] as HashMap<*, *>
                            val travelDistances = aggregateResults["travelDistances"] as HashMap<*, *>
                            val travelDistance = travelDistances[range] as HashMap<*, *>
                            val totBus = travelDistance["bus"] as Long
                            val totTrain = travelDistance["train"] as Long
                            val totWalking = travelDistance["walking"] as Long
                            val totCar = travelDistance["car"] as Long
                            if ((totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble()) != 0.0) {
                                userWeightedAverage =
                                    (totBus.toDouble() * busScore + totTrain.toDouble() * trainScore + totWalking.toDouble() * walkingScore + totCar.toDouble() * carScore) /
                                            (totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble())
                            }
                            println("User Weighted Average: $userWeightedAverage")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    exception.message?.let { util.showToast(it) }
                }
        }

        val green = if (userWeightedAverage >= generalWeightedAverage){
            "Great! You are very green, keep it up!"
        } else {
            "Bad! You are below the general average."
        }
        if (userID != null) {
            val userRef = db.collection("users").document(userID)
            userRef.get()
                .addOnSuccessListener { document ->
                    val resultToUpdate = getFinalUserResultToUpdate()
                    val docRef = document.reference
                    docRef.update(resultToUpdate, green)
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    exception.message?.let { util.showToast(it) }
                }
        }
    }

    private fun initializeAggregateResults() : AggregateResults {
        lateinit var aggregateResults: AggregateResults
        if (meansOfTransportDetected == "still") {
            aggregateResults = AggregateResults(
                city = gpsHandler.startCity,
                travelDistances = mapOf(
                    "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                    "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                    "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                    "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                )
            )
            return aggregateResults
        }
        if (finalDistance >= 0 && finalDistance < 1){
            when(meansOfTransportDetected){
                "bus" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "car" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "train" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "walking" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
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
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "car" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "train" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "walking" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
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
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "car" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "train" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "walking" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
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
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 1, "car" to 0, "train" to 0, "walking" to 0)
                    )
                )
                "car" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 1, "train" to 0, "walking" to 0)
                    )
                )
                "train" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 1, "walking" to 0)
                    )
                )
                "walking" -> aggregateResults = AggregateResults(
                    city = gpsHandler.startCity,
                    travelDistances = mapOf(
                        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
                        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 1)
                    )
                )
            }
        }
        return aggregateResults
    }

    private fun getFinalUserResultToUpdate() : String {
        lateinit var finalResult: String
        if (finalDistance >= 0 && finalDistance < 1){
            finalResult = "last_<1km"
        } else if(finalDistance >= 1 && finalDistance < 5) {
            finalResult = "last_1-5km"
        } else if(finalDistance >= 5 && finalDistance < 10) {
            finalResult = "last_5-10km"
        } else if(finalDistance >= 10) {
            finalResult = "last_>10km"
        }
        return finalResult
    }

    private fun getRange() : String {
        lateinit var range : String
        if(finalDistance >= 0 && finalDistance < 1){
            range = "range(<1km)"
        } else if(finalDistance >= 1 && finalDistance < 5) {
            range = "range(1-5km)"
        } else if(finalDistance >= 5 && finalDistance < 10) {
            range = "range(5-10km)"
        } else if(finalDistance >= 10) {
            range = "range(>10km)"
        }
        return range
    }

    private fun initializeFieldPath() : String {
        lateinit var fieldPath: String
        if(finalDistance >= 0 && finalDistance < 1){
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
        return fieldPath
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing to disable the button
    }

}

