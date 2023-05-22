package it.unipi.dii.masss_project.database

import android.content.ContentValues
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import it.unipi.dii.masss_project.Util

class FirestoreManager {
    // initialize firebase firestore
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val carScore = 1
    private val busScore = 5
    private val walkingScore = 10
    private val trainScore = 7

    fun createUserDocument(userId: String, email: String, password: String, callback: (Boolean) -> Unit) {
        val userRef: DocumentReference = db.collection("users").document(userId)
        val parts = email.split("@")
        val username: String = parts[0]
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "password" to password,
            "last_<1km" to "",
            "last_1-5km" to "",
            "last_5-10km" to "",
            "last_>10km" to ""
        )
        userRef.set(userData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun createNewDocumentAggregateResults(
        meansOfTransportDetected: String,
        finalDistance: Double,
        startCity: String
    ) {
        val aggregateResults = initializeAggregateResults(meansOfTransportDetected, finalDistance, startCity)
        val aggregateResultsRef = db.collection("aggregateResults").document(startCity)
        aggregateResultsRef.set(aggregateResults)
    }

    private fun updateDocumentAggregateResults(
        meansOfTransportDetected: String,
        finalDistance: Double,
        documents: QuerySnapshot
    ) {
        val increment = FieldValue.increment(1)
        val fieldPath = initializeFieldPath(meansOfTransportDetected, finalDistance)
        for (document in documents) {
            val docRef = document.reference
            docRef.update(fieldPath, increment)
                .addOnSuccessListener {
                    Log.d(
                        ContentValues.TAG,
                        "Incremented $fieldPath value of aggregateResults collection"
                    )
                }
                .addOnFailureListener { e ->
                    Log.w(
                        ContentValues.TAG,
                        "Error incrementing $fieldPath value of aggregateResults collection",
                        e
                    )
                }

        }
    }

    private fun updateUserAggregateResults(
        meansOfTransportDetected: String,
        finalDistance: Double,
        startCity: String,
        document: DocumentSnapshot
    ) {
        val increment = FieldValue.increment(1)
        val fieldPath = "results.${startCity}." + initializeFieldPath(meansOfTransportDetected, finalDistance)
        val docRef = document.reference
        docRef.update(fieldPath, increment)
            .addOnSuccessListener {
                Log.d(
                    ContentValues.TAG,
                    "Incremented $fieldPath value of user collection"
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    ContentValues.TAG,
                    "Error incrementing $fieldPath value of user collection",
                    e
                )
            }
    }

    private fun insertNewUserAggregateResults(
        meansOfTransportDetected: String,
        finalDistance: Double,
        startCity: String,
        document: DocumentSnapshot
    ){
        val aggregateResults = initializeAggregateResults(meansOfTransportDetected, finalDistance, startCity)
        val docRef = document.reference
        docRef.update("results.${startCity}", aggregateResults)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "Added new aggregate results for new city in user collection") }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error adding new aggregate results for new city in user collection", e) }
    }

    private fun createUserAggregateResults(
        meansOfTransportDetected: String,
        finalDistance: Double,
        startCity: String,
        document: DocumentSnapshot
    ){
        val aggregateResults = initializeAggregateResults(meansOfTransportDetected, finalDistance, startCity)
        val results = hashMapOf(
            startCity to aggregateResults
        )
        val docRef = document.reference
        docRef.update("results",results)
            .addOnSuccessListener { Log.d(ContentValues.TAG, "Results added to user document") }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error adding results to user document", e) }
    }

    fun updateStorage(
        userId: String?,
        meansOfTransportDetected: String,
        startCity: String,
        finalDistance: Double,
        util: Util
    ) {

        // retrieve and update aggregate results for start city, if any
        val query = db.collection("aggregateResults").whereEqualTo("city", startCity)
        query.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // No documents found for start city -> create a new document
                createNewDocumentAggregateResults(meansOfTransportDetected, finalDistance, startCity)

            } else {
                if (meansOfTransportDetected != "still") {
                    // Document(s) found for start city -> update document
                    updateDocumentAggregateResults(meansOfTransportDetected, finalDistance, documents)
                }
            }
        }.addOnFailureListener { exception ->
            exception.message?.let { util.showToast(it) }
        }

        if (userId != null) {
            // retrieve user document to update its aggregate results
            val userRef = db.collection("users").document(userId)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.data?.containsKey("results") == true) {
                        // the user document contains aggregate results
                        val results = document.data?.get("results") as HashMap<*, *>

                        // get aggregate user results for start city, if any
                        if (results.containsKey(startCity)) {
                            if (meansOfTransportDetected != "still") {
                                // user have aggregate results for start city -> update aggregate results
                                updateUserAggregateResults(meansOfTransportDetected, finalDistance, startCity, document)
                            }
                        } else {
                            // user have no aggregate results for start city -> insert new aggregate results in results map
                            insertNewUserAggregateResults(meansOfTransportDetected, finalDistance, startCity, document)
                        }
                    } else {
                        // the user haven't any aggregate results yet -> create results field
                        createUserAggregateResults(meansOfTransportDetected, finalDistance, startCity, document)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    exception.message?.let { util.showToast(it) }
                }
        }

        // calculate green score
        if(meansOfTransportDetected != "still") {
            var generalWeightedAverage = 0.0
            var userWeightedAverage: Double
            val query2 = db.collection("aggregateResults").whereEqualTo("city", startCity)
            query2.get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    generalWeightedAverage = getGeneralWeightedAverage(finalDistance, documents)
                }
                if (userId != null) {
                    val userRef = db.collection("users").document(userId)
                    userRef.get()
                        .addOnSuccessListener { document ->
                            userWeightedAverage =
                                getUserWeightedAverage(startCity, finalDistance, document)
                            updateUserGreenScore(
                                userWeightedAverage,
                                generalWeightedAverage,
                                finalDistance,
                                document
                            )
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors here
                            exception.message?.let { util.showToast(it) }
                        }
                }
            }.addOnFailureListener { exception ->
                exception.message?.let { util.showToast(it) }
            }
        }
    }

    private fun updateUserGreenScore(
        userWeightedAverage: Double,
        generalWeightedAverage: Double,
        finalDistance: Double,
        document: DocumentSnapshot
    ) {
        val green = if (userWeightedAverage >= generalWeightedAverage){
            "Great! You are very green, keep it up!"
        } else {
            "Bad! You are below the general average."
        }
        val resultToUpdate = getFinalUserResultToUpdate(finalDistance)
        val docRef = document.reference
        docRef.update(resultToUpdate, green)
    }

    private fun getGeneralWeightedAverage(finalDistance: Double, documents: QuerySnapshot): Double {
        var generalWeightedAverage = 0.0
        for (document in documents) {
            val range = getRange(finalDistance)
            val travelDistance = document.get("travelDistances.$range") as HashMap<*, *>
            val totBus = travelDistance["bus"] as Long
            val totTrain = travelDistance["train"] as Long
            val totWalking = travelDistance["walking"] as Long
            val totCar = travelDistance["car"] as Long
            generalWeightedAverage =
                if ((totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble()) != 0.0) {
                    (totBus.toDouble() * busScore + totTrain.toDouble() * trainScore + totWalking.toDouble() * walkingScore + totCar.toDouble() * carScore) /
                            (totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble())
                } else {
                    0.0
                }
            println("General Weighted Average: $generalWeightedAverage")
        }
        return generalWeightedAverage
    }

    private fun getUserWeightedAverage(
        startCity: String,
        finalDistance: Double,
        document: DocumentSnapshot
    ): Double{
        var userWeightedAverage = 0.0
        if (document.data?.containsKey("results") == true) {
            val results = document.data?.get("results") as HashMap<*, *>
            if (results.containsKey(startCity)) {
                val range = getRange(finalDistance)
                val aggregateResults = results[startCity] as HashMap<*, *>
                val travelDistances = aggregateResults["travelDistances"] as HashMap<*, *>
                val travelDistance = travelDistances[range] as HashMap<*, *>
                val totBus = travelDistance["bus"] as Long
                val totTrain = travelDistance["train"] as Long
                val totWalking = travelDistance["walking"] as Long
                val totCar = travelDistance["car"] as Long
                userWeightedAverage = if ((totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble()) != 0.0) {
                    (totBus.toDouble() * busScore + totTrain.toDouble() * trainScore + totWalking.toDouble() * walkingScore + totCar.toDouble() * carScore) /
                            (totBus.toDouble() + totTrain.toDouble() + totWalking.toDouble() + totCar.toDouble())
                } else {
                    0.0
                }
                println("User Weighted Average: $userWeightedAverage")
            }
        }
        return userWeightedAverage
    }

    private fun initializeAggregateResults(meansOfTransportDetected: String, finalDistance: Double, startCity: String) : AggregateResults {
        lateinit var aggregateResults: AggregateResults
        if (meansOfTransportDetected == "still") {
            aggregateResults = AggregateResults(
                city = startCity,
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
        return aggregateResults
    }

    private fun getFinalUserResultToUpdate(finalDistance: Double) : String {
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

    private fun getRange(finalDistance: Double) : String {
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

    private fun initializeFieldPath(meansOfTransportDetected: String, finalDistance: Double) : String {
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

    fun getUserResultRange(userId: String?, range: String , onSuccess: (String?) -> Unit, onFailure: (String) -> Unit) {
        if (userId != null) {
            // Get a reference to the user document
            val userRef = db.collection("users").document(userId)
            // Get the user data
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val result = document.getString(range)
                        onSuccess(result)
                    } else {
                        onFailure("User document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception.message ?: "Unknown error occurred")
                }
        }
    }
}