package it.unipi.dii.masss_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import it.unipi.dii.masss_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)

        val button: Button = binding.loginButton
        button.setOnClickListener {onLoginAttempt(binding)}
    }

    private fun onLoginAttempt(binding: ActivityMainBinding) {
        val intent = Intent(this, RecordingActivity::class.java)

        val username: String = binding.inputUsername.text.toString()
        val email: String = binding.inputEmail.text.toString()
        val password: String = binding.inputPassword.text.toString()

        if(username != "" || password != "" || email!= "") {
            // TODO: Aggiungi login a Firebased (Database)
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        var user = auth.currentUser

                        // Pass the inserted username to the intent
                        intent.putExtra("username", username)
                        startActivity(intent)
                    } else {
                        // Otherwise show error message
                        val message = "Authentication failed: ${task.exception}"
                        val duration = Toast.LENGTH_LONG

                        val toast = Toast.makeText(this, message, duration)
                        toast.show()
                    }
                }



        }
        else{
            // Otherwise show error message
            val message = "Insert username, email and password, please"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this, message, duration)
            toast.show()
        }

    }

    /*override fun onResume() : Unit
        super.onResume()

    }*/

}