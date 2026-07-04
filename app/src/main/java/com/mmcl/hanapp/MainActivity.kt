package com.mmcl.hanapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivityMainBinding
import com.mmcl.hanapp.ui.discovered.DiscoveredFragment
import com.mmcl.hanapp.ui.finding.FindingFragment

// Main screen: hosts the two tabs (Discovered / Finding) and the logout control.
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    private val tabTitles = arrayOf("Discovered", "Finding")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Safety net: if somehow this screen is reached with no logged-in user
        // (e.g. session cleared), bounce back to login rather than showing a broken state.
        if (!session.isLoggedIn()) {
            goToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        setupLogout()
    }

    // Connects the tab strip to the swipeable pager.
    private fun setupTabs() {
        val adapter = NewsfeedPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    // Wires the header logout icon to a confirmation dialog before actually logging out.
    private fun setupLogout() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    // Asks the user to confirm, so an accidental tap doesn't wipe their session mid-use.
    private fun showLogoutConfirmation() {
        // Pass the custom dialog theme so the action buttons render in visible sky-blue.
        AlertDialog.Builder(this, R.style.HanAppAlertDialog)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_message)
            .setPositiveButton(R.string.logout_confirm) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.logout_cancel, null)
            .show()
    }

    // Clears the session and returns to the login screen.
    private fun performLogout() {
        session.logout()
        goToLogin()
    }

    // Sends the user to login and closes this screen so back-press can't return here
    // while logged out.
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        // Clear the activity history so the app can't navigate "back" into a logged-out session.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Supplies the two tab fragments to the pager.
    private inner class NewsfeedPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DiscoveredFragment.newInstance()
                1 -> FindingFragment.newInstance()
                else -> throw IllegalArgumentException("Invalid tab position: $position")
            }
        }
    }
}