package it.unipi.dii.masss_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
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
        // Pass the inserted username to the intent
        val username: String = binding.inputUsername.text.toString()
        val email: String = binding.inputEmail.text.toString()
        val password: String = binding.inputPassword.text.toString()
        if(username != "" || password != "" || email!= "") {
            intent.putExtra("username", username)
            startActivity(intent)
            // TODO: Aggiungi login a Firebased (Database)
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