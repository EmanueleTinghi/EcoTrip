package it.unipi.dii.masss_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding
import it.unipi.dii.masss_project.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_result)

        username = intent.getStringExtra("username").toString()

        // todo : aggiungere il testo del risutato ottenuto
        val resultTextView: TextView = binding.resultTextView
        resultTextView.text = ""
        resultTextView.visibility = View.VISIBLE

        val backButton = binding.backButton
        backButton.setOnClickListener { onBackPressed(binding) }
    }

    private fun onBackPressed(binding: ActivityResultBinding) {
        val intent = Intent(this, RecordingActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}