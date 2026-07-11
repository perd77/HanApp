package com.mmcl.hanapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter as ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mmcl.hanapp.R
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.FragmentHomeBinding
import com.mmcl.hanapp.ui.discovered.DiscoveredFragment
import com.mmcl.hanapp.ui.finding.FindingFragment

// The Home destination: hosts the header (title + logout) and the two feed
// tabs. Also owns TabFilterViewModel, shared with both child feed fragments,
// so re-tapping the active tab can toggle "My Posts" mode inside it.
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager

    // Scoped to THIS fragment (Home) — DiscoveredFragment and FindingFragment
    // access the same instance by scoping to requireParentFragment(), which
    // resolves to this HomeFragment. That shared instance is what connects
    // a tap here to a filter change inside whichever child is showing.
    private val tabFilterViewModel: TabFilterViewModel by viewModels()

    private val tabTitles = arrayOf("Discovered", "Finding")

    companion object {
        private const val ARG_INITIAL_TAB = "initial_tab"

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
        setupTabReselectListener()
    }

    private fun setupGreeting() {
        val username = session.getUsername().orEmpty()
        binding.textUserGreeting.text = getString(R.string.home_greeting, username)
    }

    private fun setupTabs() {
        binding.viewPager.adapter = FeedPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        val initialTab = arguments?.getInt(ARG_INITIAL_TAB, 0) ?: 0
        if (initialTab != 0) {
            binding.viewPager.setCurrentItem(initialTab, false)
        }
    }

    // Detects a tap on the ALREADY-selected tab (not a normal tab switch) and
    // toggles that tab's "My Posts" filter via the shared TabFilterViewModel.
    private fun setupTabReselectListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Normal tab switch — nothing to do here, ViewPager2 handles it.
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // No action needed.
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> tabFilterViewModel.toggleDiscovered()
                    1 -> tabFilterViewModel.toggleFinding()
                }
            }
        })
    }

    private fun setupLogout() {
        binding.buttonLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.HanAppAlertDialog)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_message)
            .setPositiveButton(R.string.logout_confirm) { _, _ -> performLogout() }
            .setNegativeButton(R.string.logout_cancel, null)
            .show()
    }

    private fun performLogout() {
        session.logout()
        (activity as? MainActivityCallback)?.onLoggedOut()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class FeedPagerAdapter(fragment: Fragment) : ViewPagerAdapter(fragment) {
        override fun getItemCount(): Int = tabTitles.size
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> DiscoveredFragment.newInstance()
            1 -> FindingFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    interface MainActivityCallback {
        fun onLoggedOut()
    }
}