package com.mmcl.hanapp.ui.discovered

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mmcl.hanapp.databinding.FragmentDiscoveredBinding

class DiscoveredFragment : Fragment() {

    private var _binding: FragmentDiscoveredBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = DiscoveredFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoveredBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewDiscovered.layoutManager = LinearLayoutManager(requireContext())

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}