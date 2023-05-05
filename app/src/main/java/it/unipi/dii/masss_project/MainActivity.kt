package it.unipi.dii.masss_project

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import it.unipi.dii.masss_project.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val button: Button = binding.loginButton
        button.setOnClickListener{onLoginAttempt()}

        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()
    }

    private fun onLoginAttempt() {
        val email: String = binding.inputEmail.text.toString()
        val password: String = binding.inputPassword.text.toString()

        if(password.isNotEmpty() && email.isNotEmpty()) {

            auth = FirebaseAuth.getInstance()

            // check if user exists
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val result = task.result?.signInMethods
                        if (result?.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) == true) {
                            // user exists
                            // handle login flow
                            loginUser(email, password)

                        } else {
                            // user doesn't exist
                            // handle registration flow
                            registerUser(email, password)
                        }
                    } else {
                        // handle error
                        val message = "Error in checking user existence"
                        val duration = Toast.LENGTH_LONG

                        val toast = Toast.makeText(this, message, duration)

                        toast.show()

                        val errorTextView: TextView = binding.errorTextView
                        errorTextView.text = "${task.exception?.message}"
                        errorTextView.setTextColor(Color.RED)
                        errorTextView.visibility = View.VISIBLE
                    }
                }
        }
        else{
            // Otherwise show error message
            val message = "Insert email and password, please"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this, message, duration)

            toast.show()
        }

    }

    override fun onStop() {
        super.onStop()

        // Save the text to SharedPreferences
        val prefs = getPreferences(MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("password", binding.inputPassword.text.toString())
        editor.putString("email", binding.inputEmail.text.toString())
        editor.apply()
    }

    override fun onRestart(){
        super.onRestart()

        // Set the input fields to previous values
        val prefs = getPreferences(MODE_PRIVATE)
        binding.inputPassword.setText(prefs.getString("password", ""))
        binding.inputEmail.setText(prefs.getString("email", ""))

    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // go on recordingActivity
                    startRecordingActivity(email)
                } else {
                    // Otherwise show error message
                    val message = "Authentication failed"
                    val duration = Toast.LENGTH_LONG

                    val toast = Toast.makeText(this, message, duration)
                    toast.show()

                    val errorTextView: TextView = binding.errorTextView
                    errorTextView.text = "${task.exception?.message}"
                    errorTextView.setTextColor(Color.RED)
                    errorTextView.visibility = View.VISIBLE

                }
            }
    }

    private fun startRecordingActivity(email: String) {
        // Pass the inserted username to the intent
        val parts = email.split("@")
        val username: String = parts[0]
        val intent = Intent(this, RecordingActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // generate a db associated with the new registered user
                    val user: FirebaseUser? = auth.currentUser
                    val userId: String? = user?.uid
                    val userRef: DocumentReference = db.collection("users").document(userId!!)
                    val parts = email.split("@")
                    val username: String = parts[0]
                    val userData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "password" to password
                    )
                    userRef.set(userData)

                    // go on recordingActivity
                    startRecordingActivity(email)
                } else {
                    // Otherwise show error message
                    val message = "Registration failed"
                    val duration = Toast.LENGTH_LONG

                    val toast = Toast.makeText(this, message, duration)
                    toast.show()

                    val errorTextView: TextView = binding.errorTextView
                    errorTextView.text = "${task.exception?.message}"
                    errorTextView.setTextColor(Color.RED)
                    errorTextView.visibility = View.VISIBLE

                }
            }
    }

}