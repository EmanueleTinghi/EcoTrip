package it.unipi.dii.masss_project

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.unipi.dii.masss_project.databinding.ActivityResultBinding
import it.unipi.dii.masss_project.databinding.Schedule1Binding
import it.unipi.dii.masss_project.databinding.Schedule2Binding
import it.unipi.dii.masss_project.databinding.Schedule3Binding
import it.unipi.dii.masss_project.databinding.Schedule4Binding

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
        val resultTextView: TextView = binding.resultTextView
        "$username, this are your results:\n".also { resultTextView.text = it }
        resultTextView.visibility = View.VISIBLE

        // register listener for backButton
        val backButton = binding.backButton
        backButton.setOnClickListener { onBackButtonPressed() }

        // add listener for logoutButton
        val logoutButton: ImageButton = binding.logoutButton2
        logoutButton.setOnClickListener {onLogout() }

        val viewPager = binding.viewPager
        viewPager.adapter = SchedulePagerAdapter(supportFragmentManager)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)

    }

    private fun onBackButtonPressed() {
        val intent = Intent(this, RecordingActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    private fun onLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}

class SchedulePagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> Schedule1Fragment()
            1 -> Schedule2Fragment()
            2 -> Schedule3Fragment()
            3 -> Schedule4Fragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "<1Km trip"
            1 -> "1-5Km trip"
            2 -> "5-10Km trip"
            3 -> ">10Km trip"
            else -> null
        }
    }
}

/** ************************************************************************************************
 *  Schedule class inserted into view Pager
 */
class Schedule1Fragment : Fragment() {

    private lateinit var binding: Schedule1Binding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule1Binding.inflate(inflater, container, false)

        // initialize firebase authentication
        auth = FirebaseAuth.getInstance()
        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()

        // Get the currently signed-in user
        val currentUser = auth.currentUser

        // Retrieve the user ID
        val userID = currentUser?.uid

        if (userID != null) {
            // Get a reference to the user document
            val userRef = db.collection("users").document(userID)

            // Get the user data
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val result = document.getString("last_<1km")

                        // add text view to see result
                        val resultTextView: TextView = binding.textViewSchedule1

                        if( result!= null) {
                            "$result".also { resultTextView.text = it }
                        } else {
                            "you haven't done any <1km trips yet".also { resultTextView.text = it }
                        }

                        when (result) {
                            "Great! You are very green, keep it up!" -> {
                                val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                                resultTextView.setTextColor(resolvedColor)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.VISIBLE
                            }
                            "Bad! You are below the general average." -> {
                                resultTextView.setTextColor(Color.RED)

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.VISIBLE
                            }
                            else -> {
                                resultTextView.setTextColor(Color.RED)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.INVISIBLE

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.INVISIBLE
                            }
                        }

                        resultTextView.visibility = View.VISIBLE

                    } else {
                        // User document does not exist
                        val message = "User document does not exist"
                        val duration = Toast.LENGTH_LONG
                        val toast = Toast.makeText(context, message, duration)
                        toast.show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    val message = "${exception.message}"
                    val duration = Toast.LENGTH_LONG
                    val toast = Toast.makeText(context, message, duration)
                    toast.show()
                }
        }
        return binding.root
    }
}

class Schedule2Fragment : Fragment() {

    private lateinit var binding: Schedule2Binding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule2Binding.inflate(inflater, container, false)

        // initialize firebase authentication
        auth = FirebaseAuth.getInstance()
        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()

        // Get the currently signed-in user
        val currentUser = auth.currentUser

        // Retrieve the user ID
        val userID = currentUser?.uid

        if (userID != null) {
            // Get a reference to the user document
            val userRef = db.collection("users").document(userID)

            // Get the user data
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val result = document.getString("last_1-5km")

                        // add text view to see result
                        val resultTextView: TextView = binding.textViewSchedule2

                        if( result!= null) {
                            "$result".also { resultTextView.text = it }
                        } else {
                            "you haven't done any 1-5km trips yet".also { resultTextView.text = it }
                        }

                        when (result) {
                            "Great! You are very green, keep it up!" -> {
                                val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                                resultTextView.setTextColor(resolvedColor)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.VISIBLE
                            }

                            "Bad! You are below the general average." -> {
                                resultTextView.setTextColor(Color.RED)

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.VISIBLE
                            } else -> {
                                resultTextView.setTextColor(Color.RED)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.INVISIBLE

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.INVISIBLE
                            }
                        }
                        resultTextView.visibility = View.VISIBLE
                    } else {
                        // User document does not exist
                        val message = "User document does not exist"
                        val duration = Toast.LENGTH_LONG
                        val toast = Toast.makeText(context, message, duration)
                        toast.show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    val message = "${exception.message}"
                    val duration = Toast.LENGTH_LONG
                    val toast = Toast.makeText(context, message, duration)
                    toast.show()
                }
        }
        return binding.root
    }
}

class Schedule3Fragment : Fragment() {

    private lateinit var binding: Schedule3Binding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule3Binding.inflate(inflater, container, false)

        // initialize firebase authentication
        auth = FirebaseAuth.getInstance()
        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()

        // Get the currently signed-in user
        val currentUser = auth.currentUser

        // Retrieve the user ID
        val userID = currentUser?.uid

        if (userID != null) {
            // Get a reference to the user document
            val userRef = db.collection("users").document(userID)

            // Get the user data
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val result = document.getString("last_5-10km")

                        // add text view to see result
                        val resultTextView: TextView = binding.textViewSchedule3

                        if( result!= null) {
                            "$result".also { resultTextView.text = it }
                        } else {
                            "you haven't done any 5-10km trips yet".also { resultTextView.text = it }
                        }

                        when (result) {
                            "Great! You are very green, keep it up!" -> {
                                val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                                resultTextView.setTextColor(resolvedColor)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.VISIBLE
                            }
                            "Bad! You are below the general average." -> {
                                resultTextView.setTextColor(Color.RED)

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.VISIBLE
                            }
                            else -> {
                                resultTextView.setTextColor(Color.RED)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.INVISIBLE

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.INVISIBLE
                            }
                        }

                        resultTextView.visibility = View.VISIBLE
                    } else {
                        // User document does not exist
                        val message = "User document does not exist"
                        val duration = Toast.LENGTH_LONG
                        val toast = Toast.makeText(context, message, duration)
                        toast.show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    val message = "${exception.message}"
                    val duration = Toast.LENGTH_LONG
                    val toast = Toast.makeText(context, message, duration)
                    toast.show()
                }
        }
        return binding.root
    }
}

class Schedule4Fragment : Fragment() {

    private lateinit var binding: Schedule4Binding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule4Binding.inflate(inflater, container, false)

        // initialize firebase authentication
        auth = FirebaseAuth.getInstance()
        // initialize firebase firestore
        db = FirebaseFirestore.getInstance()

        // Get the currently signed-in user
        val currentUser = auth.currentUser

        // Retrieve the user ID
        val userID = currentUser?.uid

        if (userID != null) {
            // Get a reference to the user document
            val userRef = db.collection("users").document(userID)

            // Get the user data
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val result = document.getString("last_>10km")

                        // add text view to see result
                        val resultTextView: TextView = binding.textViewSchedule4

                        if( result!= null) {
                            "$result".also { resultTextView.text = it }
                        } else {
                            "you haven't done any >10km trips yet".also { resultTextView.text = it }
                        }

                        when (result) {
                            "Great! You are very green, keep it up!" -> {
                                val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                                resultTextView.setTextColor(resolvedColor)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.VISIBLE
                            }
                            "Bad! You are below the general average." -> {
                                resultTextView.setTextColor(Color.RED)

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.VISIBLE
                            }
                            else -> {
                                resultTextView.setTextColor(Color.RED)

                                val likeImageView = binding.likeImageView
                                likeImageView.visibility = View.INVISIBLE

                                val dislikeImageView = binding.dislikeImageView
                                dislikeImageView.visibility = View.INVISIBLE
                            }
                        }

                        resultTextView.visibility = View.VISIBLE
                    } else {
                        // User document does not exist
                        val message = "User document does not exist"
                        val duration = Toast.LENGTH_LONG
                        val toast = Toast.makeText(context, message, duration)
                        toast.show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                    val message = "${exception.message}"
                    val duration = Toast.LENGTH_LONG
                    val toast = Toast.makeText(context, message, duration)
                    toast.show()
                }
        }
        return binding.root
    }
}
