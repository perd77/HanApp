package com.mmcl.hanapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mmcl.hanapp.databinding.ActivityMainBinding
import com.mmcl.hanapp.ui.discovered.DiscoveredFragment
import com.mmcl.hanapp.ui.finding.FindingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tabTitles = arrayOf("Discovered", "Finding")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
    }

    private fun setupTabs() {
        val adapter = NewsfeedPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

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