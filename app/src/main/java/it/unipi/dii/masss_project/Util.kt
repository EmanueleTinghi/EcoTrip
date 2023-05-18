package it.unipi.dii.masss_project

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import it.unipi.dii.masss_project.databinding.ActivityMainBinding

class Util(context: Context, binding: ActivityMainBinding) {
    // initialize application context
    private val appContext = context

    // initialize activity main binding
    private val appMainBinding = binding

    fun showErrorToast(message: String) {
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(appContext, message, duration)
        toast.show()
    }

    fun showErrorTextView(message: String){
        val errorTextView: TextView = appMainBinding.errorTextView
        errorTextView.text = message
        errorTextView.setTextColor(Color.RED)
        errorTextView.visibility = View.VISIBLE
    }
}