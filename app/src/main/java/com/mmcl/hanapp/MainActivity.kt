package com.mmcl.hanapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivityMainBinding
import com.mmcl.hanapp.ui.home.HomeFragment
import com.mmcl.hanapp.ui.notifications.NotificationsFragment

// App shell: hosts the bottom bar + docked FAB, and swaps the content area
// between the Home feed and the Notifications screen.
// Implements HomeFragment's callback so it can handle logout navigation.
class MainActivity : AppCompatActivity(), HomeFragment.MainActivityCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    // Launches PostItemActivity and listens for its result. If a post was
    // created successfully, Home is reloaded fresh so both feeds show the
    // new item immediately — no manual pull-to-refresh needed.
    private val postItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val postType = result.data?.getStringExtra(PostItemActivity.EXTRA_POST_TYPE)
            val tabToShow = if (postType == "LOST") 1 else 0
            showFragment(HomeFragment.newInstance(tabToShow))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Safety net: if reached without a logged-in user, bounce to login.
        if (!session.isLoggedIn()) {
            goToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show the Home feed by default on first launch (not on rotation, to
        // avoid stacking duplicate fragments).
        if (savedInstanceState == null) {
            showFragment(HomeFragment.newInstance())
        }

        setupBottomNav()
        setupFab()
    }

    // Switches the content area based on which bottom-bar icon is tapped.
    private fun setupBottomNav() {
        binding.navHome.setOnClickListener {
            showFragment(HomeFragment.newInstance())
        }
        binding.navNotifications.setOnClickListener {
            showFragment(NotificationsFragment.newInstance())
        }
    }

    // The docked "+" opens the post-creation flow via the result launcher,
    // so we know when a post was successfully created and can refresh Home.
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            postItemLauncher.launch(Intent(this, PostItemActivity::class.java))
        }
    }

    // Replaces whatever is in the content container with the given fragment.
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(binding.navHostContainer.id, fragment)
        }
    }

    // Called by HomeFragment after the user confirms logout.
    override fun onLoggedOut() {
        goToLogin()
    }

    // Sends the user back to login and clears the back stack so they can't
    // navigate back into a logged-out session.
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}