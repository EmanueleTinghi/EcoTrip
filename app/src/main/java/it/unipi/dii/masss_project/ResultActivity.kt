package it.unipi.dii.masss_project

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import it.unipi.dii.masss_project.databinding.ActivityResultBinding
import it.unipi.dii.masss_project.databinding.Schedule1Binding
import it.unipi.dii.masss_project.databinding.Schedule2Binding
import it.unipi.dii.masss_project.databinding.Schedule3Binding
import it.unipi.dii.masss_project.databinding.Schedule4Binding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var username: String

    private lateinit var authManager : FirebaseAuthManager

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

        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(this, null, null)

    }

    private fun onBackButtonPressed() {
        val intent = Intent(this, RecordingActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    private fun onLogout() {
        authManager.logoutUser()
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

    private lateinit var util: Util

    private lateinit var authManager : FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule1Binding.inflate(inflater, container, false)

        util = context?.let { Util(it, null) }!!

        // initialize firebase firestore manager
        firestoreManager = FirestoreManager()
        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(null, null, firestoreManager)

        // Retrieve the user ID
        val userID = authManager.getUserID()

        if (userID != null) {
            firestoreManager.getUserResultRange(userID, "last_<1km",
            onSuccess = { result ->
                // add text view to see result
                val resultTextView: TextView = binding.textViewSchedule1

                if( result != null && result != "") {
                    "$result".also { resultTextView.text = it }
                } else {
                    "You haven't done any <1km trips yet".also { resultTextView.text = it }
                }

                when (result) {
                    "Great! You are very green, keep it up!" -> {
                        val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                        resultTextView.setTextColor(resolvedColor)

                        val likeImageView = binding.likeImageView
                        likeImageView.visibility = View.VISIBLE

                        val dislikeImageView = binding.dislikeImageView
                        dislikeImageView.visibility = View.INVISIBLE
                    }
                    "Bad! You are below the general average." -> {
                        resultTextView.setTextColor(Color.RED)

                        val dislikeImageView = binding.dislikeImageView
                        dislikeImageView.visibility = View.VISIBLE

                        val likeImageView = binding.likeImageView
                        likeImageView.visibility = View.INVISIBLE
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

            },
            onFailure = { errorMessage ->
                util.showToast(errorMessage)
            })

        }
        return binding.root
    }
}

class Schedule2Fragment : Fragment() {

    private lateinit var binding: Schedule2Binding

    private lateinit var util: Util

    private lateinit var authManager : FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule2Binding.inflate(inflater, container, false)

        util = context?.let { Util(it, null) }!!

        // initialize firebase firestore manager
        firestoreManager = FirestoreManager()
        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(null, null, firestoreManager)

        // Retrieve the user ID
        val userID = authManager.getUserID()

        if (userID != null) {
            firestoreManager.getUserResultRange(userID, "last_1-5km",
                onSuccess = { result ->
                    // add text view to see result
                    val resultTextView: TextView = binding.textViewSchedule2

                    if( result != null && result != "") {
                        "$result".also { resultTextView.text = it }
                    } else {
                        "You haven't done any 1-5km trips yet".also { resultTextView.text = it }
                    }

                    when (result) {
                        "Great! You are very green, keep it up!" -> {
                            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                            resultTextView.setTextColor(resolvedColor)

                            val likeImageView = binding.likeImageView
                            likeImageView.visibility = View.VISIBLE

                            val dislikeImageView = binding.dislikeImageView
                            dislikeImageView.visibility = View.INVISIBLE
                        }
                        "Bad! You are below the general average." -> {
                            resultTextView.setTextColor(Color.RED)

                            val dislikeImageView = binding.dislikeImageView
                            dislikeImageView.visibility = View.VISIBLE

                            val likeImageView = binding.likeImageView
                            likeImageView.visibility = View.INVISIBLE
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

                },
                onFailure = { errorMessage ->
                    util.showToast(errorMessage)
                })

        }
        return binding.root
    }
}

class Schedule3Fragment : Fragment() {

    private lateinit var binding: Schedule3Binding

    private lateinit var util: Util

    private lateinit var authManager : FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule3Binding.inflate(inflater, container, false)

        util = context?.let { Util(it, null) }!!

        // initialize firebase firestore manager
        firestoreManager = FirestoreManager()
        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(null, null, firestoreManager)

        // Retrieve the user ID
        val userID = authManager.getUserID()

        if (userID != null) {
            firestoreManager.getUserResultRange(userID, "last_5-10km",
                onSuccess = { result ->
                    // add text view to see result
                    val resultTextView: TextView = binding.textViewSchedule3

                    if( result != null && result != "") {
                        "$result".also { resultTextView.text = it }
                    } else {
                        "You haven't done any 5-10km trips yet".also { resultTextView.text = it }
                    }

                    when (result) {
                        "Great! You are very green, keep it up!" -> {
                            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                            resultTextView.setTextColor(resolvedColor)

                            val likeImageView = binding.likeImageView
                            likeImageView.visibility = View.VISIBLE

                            val dislikeImageView = binding.dislikeImageView
                            dislikeImageView.visibility = View.INVISIBLE
                        }
                        "Bad! You are below the general average." -> {
                            resultTextView.setTextColor(Color.RED)

                            val dislikeImageView = binding.dislikeImageView
                            dislikeImageView.visibility = View.VISIBLE

                            val likeImageView = binding.likeImageView
                            likeImageView.visibility = View.INVISIBLE
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

                },
                onFailure = { errorMessage ->
                    util.showToast(errorMessage)
                })

        }
        return binding.root
    }
}

class Schedule4Fragment : Fragment() {

    private lateinit var binding: Schedule4Binding

    private lateinit var util: Util

    private lateinit var authManager : FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = Schedule4Binding.inflate(inflater, container, false)

        util = context?.let { Util(it, null) }!!

        // initialize firebase firestore manager
        firestoreManager = FirestoreManager()
        // initialize firebase authentication manager
        authManager = FirebaseAuthManager(null, null, firestoreManager)

        // Retrieve the user ID
        val userID = authManager.getUserID()

        if (userID != null) {
            firestoreManager.getUserResultRange(userID, "last_>10km",
                onSuccess = { result ->
                    // add text view to see result
                    val resultTextView: TextView = binding.textViewSchedule4

                    if( result != null && result != "") {
                        "$result".also { resultTextView.text = it }
                    } else {
                        "You haven't done any >10km trips yet".also { resultTextView.text = it }
                    }

                    when (result) {
                        "Great! You are very green, keep it up!" -> {
                            val resolvedColor = ContextCompat.getColor(requireContext(), R.color.primary)
                            resultTextView.setTextColor(resolvedColor)

                            val likeImageView = binding.likeImageView
                            likeImageView.visibility = View.VISIBLE

                            val dislikeImageView = binding.dislikeImageView
                            dislikeImageView.visibility = View.INVISIBLE
                        }
                        "Bad! You are below the general average." -> {
                            resultTextView.setTextColor(Color.RED)

                            val dislikeImageView = binding.dislikeImageView
                            dislikeImageView.visibility = View.VISIBLE

                            val likeImageView = binding.likeImageView
                            likeImageView.visibility = View.INVISIBLE
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

                },
                onFailure = { errorMessage ->
                    util.showToast(errorMessage)
                })

        }
        return binding.root
    }
}
