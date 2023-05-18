package it.unipi.dii.masss_project

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import it.unipi.dii.masss_project.databinding.ActivityMainBinding
import it.unipi.dii.masss_project.databinding.ActivityRecordingBinding

class Util(context: Context, mainBinding: ActivityMainBinding?, recordingBinding: ActivityRecordingBinding?) {
    // initialize application context
    private val appContext = context

    // initialize activity binding
    private val appMainBinding = mainBinding
    private val appRecordingBinding = recordingBinding

    fun showToast(message: String) {
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(appContext, message, duration)
        toast.show()
    }

    fun showErrorTextView(message: String){
        if (appMainBinding != null) {
            val errorTextView: TextView = appMainBinding.errorTextView
            errorTextView.text = message
            errorTextView.setTextColor(Color.RED)
            errorTextView.visibility = View.VISIBLE
        }
    }
}