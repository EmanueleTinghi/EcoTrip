package it.unipi.dii.masss_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.loginButton)
        button.setOnClickListener {
            val intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }
    }

}