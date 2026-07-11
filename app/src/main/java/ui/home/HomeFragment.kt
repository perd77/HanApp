package com.mmcl.hanapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter as ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mmcl.hanapp.R
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.FragmentHomeBinding
import com.mmcl.hanapp.ui.discovered.DiscoveredFragment
import com.mmcl.hanapp.ui.finding.FindingFragment

// The Home destination: hosts the header (title + logout) and the two feed tabs.
// This is the content that previously lived directly in MainActivity.
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager

    private val tabTitles = arrayOf("Discovered", "Finding")

    companion object {
        private const val ARG_INITIAL_TAB = "initial_tab"

        // initialTab: 0 = Discovered, 1 = Finding. Defaults to 0 so existing
        // calls to newInstance() without an argument still open on Discovered.
        fun newInstance(initialTab: Int = 0): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply { putInt(ARG_INITIAL_TAB, initialTab) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupLogout()
        setupGreeting()
    }

    // Shows "Hi, {username}!" under the logo using the real logged-in identity.
    private fun setupGreeting() {
        val username = session.getUsername().orEmpty()
        binding.textUserGreeting.text = getString(R.string.home_greeting, username)
    }

    // Connects the tab strip to the swipeable pager holding the two feeds.
    private fun setupTabs() {
        binding.viewPager.adapter = FeedPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // If opened right after posting, jump straight to the matching tab
        // (e.g. a LOST post lands on Finding instead of the default Discovered).
        val initialTab = arguments?.getInt(ARG_INITIAL_TAB, 0) ?: 0
        if (initialTab != 0) {
            binding.viewPager.setCurrentItem(initialTab, false)
        }
    }

    // Wires the header logout icon to a confirmation dialog.
    private fun setupLogout() {
        binding.buttonLogout.setOnClickListener { showLogoutConfirmation() }
    }

    // Confirms before logging out, so an accidental tap doesn't wipe the session.
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.HanAppAlertDialog)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_message)
            .setPositiveButton(R.string.logout_confirm) { _, _ -> performLogout() }
            .setNegativeButton(R.string.logout_cancel, null)
            .show()
    }

    // Clears the session and asks the host activity to return to login.
    private fun performLogout() {
        session.logout()
        (activity as? MainActivityCallback)?.onLoggedOut()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Supplies the two feed fragments to the pager (Discovered + Finding).
    private inner class FeedPagerAdapter(fragment: Fragment) : ViewPagerAdapter(fragment) {
        override fun getItemCount(): Int = tabTitles.size
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> DiscoveredFragment.newInstance()
            1 -> FindingFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    // Lets HomeFragment tell MainActivity to handle logout navigation, without
    // HomeFragment needing to know the details of activity/intent handling.
    interface MainActivityCallback {
        fun onLoggedOut()
    }
}