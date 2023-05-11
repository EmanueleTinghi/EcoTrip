package it.unipi.dii.masss_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
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
        backButton.setOnClickListener { onBackPressed(binding) }

        // add listener for logoutButton
        val logoutButton: ImageButton = binding.logoutButton2
        logoutButton.setOnClickListener {onLogout() }

        val viewPager = binding.viewPager
        viewPager.adapter = SchedulePagerAdapter(supportFragmentManager)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)

    }

    private fun onBackPressed(binding: ActivityResultBinding) {
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
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "<1Km trip"
            1 -> "1-10Km trip"
            2 -> ">10Km trip"
            else -> null
        }
    }
}

/** ************************************************************************************************
 *  Schedule class inserted into view Pager
 */
class Schedule1Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.schedule_1, container, false)
    }
}

class Schedule2Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.schedule_2, container, false)
    }
}

class Schedule3Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.schedule_3, container, false)
    }
}
