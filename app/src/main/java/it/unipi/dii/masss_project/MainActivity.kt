package it.unipi.dii.masss_project

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import it.unipi.dii.masss_project.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var util: Util

    private lateinit var authManager : FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        util = Util(this, binding)

        // initialize firebase firestore manager
        firestoreManager = FirestoreManager()

        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(this, binding, firestoreManager)

        // add listener for loginButton
        val button: Button = binding.loginButton
        button.setOnClickListener{onLoginAttempt()}
    }

    private fun onLoginAttempt() {
        if (binding.loginButton.text == "Login") {
            // retrieve user email and password
            val email: String = binding.inputEmail.text.toString()
            val password: String = binding.inputPassword.text.toString()

            if(password.isNotEmpty() && email.isNotEmpty()) {
                // check if user exists
                authManager.checkIfUserExists(email) { exists ->
                    if (exists) {
                        // user exists
                        // handle login flow
                        authManager.loginUser(email, password) { success ->
                            if (success) {
                                startRecordingActivity(email)
                            }
                        }
                    }else {
                            // user doesn't exist
                            // handle registration flow
                            authManager.registerUser(email, password) { success ->
                                if (success) {
                                    startRecordingActivity(email)
                                }
                            }
                        }
                }
            } else {
                // Otherwise show error message
                util.showErrorToast("Insert email and password, please")
            }
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

    override fun onDestroy() {
        super.onDestroy()

        // clear input text editor
        binding.inputEmail.setText("")
        binding.inputPassword.setText("")

        // user logout
        authManager.logoutUser()
    }

    private fun startRecordingActivity(email: String) {
        // Pass the inserted username to the intent
        val parts = email.split("@")
        val username: String = parts[0]
        val intent = Intent(this, RecordingActivity::class.java)
        intent.putExtra("username", username)
        intent.putExtra("auth", email)
        startActivity(intent)
    }
}