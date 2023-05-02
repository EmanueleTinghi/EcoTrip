package it.unipi.dii.masss_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityMainBinding
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding

class RecordingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("username")

        val binding: ActivityRecordingBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_recording)

        val textView = TextView(this)
        textView.text = "Welcome ${username}!"
        val parentLayout = findViewById<ConstraintLayout>(R.id.parentLayout)
        parentLayout.addView(textView)

        val button: Button = binding.startButton
        button.setOnClickListener {onStartAttempt(binding)}
    }

    private fun onStartAttempt(binding: ActivityRecordingBinding) {

    }
}