package it.unipi.dii.masss_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.database.FirebaseAuthManager
import it.unipi.dii.masss_project.database.FirestoreManager
import it.unipi.dii.masss_project.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var util: Util

    private var numClicks = 0

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

        val logo: ImageView = binding.imageView
        logo.setOnClickListener{easterEgg()}
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
                util.showToast("Insert email and password, please")
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

    private fun easterEgg(){
        numClicks++
        if(numClicks == 3){
            val logo: ImageView = binding.imageView
            logo.animate().scaleX(-1f).withEndAction {
                logo.setImageResource(R.drawable.beerzone_logo)
                logo.animate().scaleX(1f).start()
            }.start()
        }
        if(numClicks == 4){
            val logo: ImageView = binding.imageView
            logo.animate().scaleX(-1f).withEndAction {
                logo.setImageResource(R.drawable.logo_eco)
                logo.animate().scaleX(1f).start()
            }.start()

            numClicks = 0
        }

    }
}