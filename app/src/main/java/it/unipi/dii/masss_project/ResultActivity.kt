package it.unipi.dii.masss_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding
import it.unipi.dii.masss_project.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityResultBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_result)
    }
}