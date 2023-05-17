package it.unipi.dii.masss_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_result)

        // retrieve username from intent
        username = intent.getStringExtra("username").toString()

        // add text view to see the user result
        // todo : aggiungere il testo del risutato ottenuto
        val resultTextView: TextView = binding.resultTextView
        "$username, this are your results:\n".also { resultTextView.text = it }
        resultTextView.visibility = View.VISIBLE

        // register listener for backButton
        val backButton = binding.backButton
        backButton.setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        val intent = Intent(this, RecordingActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}