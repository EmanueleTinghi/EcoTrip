package it.unipi.dii.masss_project

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager {
    // initialize firebase firestore
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

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
}