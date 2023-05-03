package it.unipi.dii.masss_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)

        val button: Button = binding.loginButton
        button.setOnClickListener{onLoginAttempt()}
    }

    private fun onLoginAttempt() {
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

    override fun onStop() {
        super.onStop()

        // Save the text to SharedPreferences
        val prefs = getPreferences(MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("username", binding.inputUsername.text.toString())
        editor.putString("password", binding.inputPassword.text.toString())
        editor.putString("email", binding.inputEmail.text.toString())
        editor.apply()
    }

    override fun onRestart(){
        super.onRestart()

        // Set the input fields to previous values
        val prefs = getPreferences(MODE_PRIVATE)
        binding.inputUsername.setText(prefs.getString("username", ""))
        binding.inputPassword.setText(prefs.getString("password", ""))
        binding.inputEmail.setText(prefs.getString("email", ""))

    }

}