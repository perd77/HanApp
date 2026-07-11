package com.mmcl.hanapp.ui.finding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.mmcl.hanapp.ItemDetailActivity
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.FragmentFindingBinding
import com.mmcl.hanapp.ui.adapter.GridSpacingItemDecoration
import com.mmcl.hanapp.ui.adapter.ItemAdapter
import com.mmcl.hanapp.ui.feed.FeedViewModel
import com.mmcl.hanapp.ui.feed.FeedViewModelFactory
import com.mmcl.hanapp.ui.home.TabFilterViewModel
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch

// The "Finding" tab: shows LOST-item posts pulled live from the backend.
// Mirrors DiscoveredFragment, including "My Posts" mode via the shared
// TabFilterViewModel, but filters for LOST.
class FindingFragment : Fragment() {

    private var _binding: FragmentFindingBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager

    private val viewModel: FeedViewModel by viewModels {
        FeedViewModelFactory(PostType.LOST, SessionManager(requireContext()).getUserId())
    }

    // Scoped to the PARENT fragment (HomeFragment), so this shares the exact
    // same TabFilterViewModel instance as HomeFragment and DiscoveredFragment.
    private val tabFilterViewModel: TabFilterViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var itemAdapter: ItemAdapter

    companion object {
        fun newInstance() = FindingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindingBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFeed()
        observeMyPostsFilter()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadItems()
        }

        binding.pillMyPosts.setOnClickListener {
            tabFilterViewModel.clearFinding()
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            val intent = ItemDetailActivity.newIntent(
                requireContext(), selectedItem, selectedItem.posterUserId
            )
            startActivity(intent)
        }

        val spacingPx = (12 * resources.displayMetrics.density).toInt()
        binding.recyclerViewFinding.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = itemAdapter
            addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingPx = spacingPx))
        }
    }

    private fun observeMyPostsFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabFilterViewModel.findingMyPostsOnly.collect { enabled ->
                    viewModel.setMyPostsOnly(enabled)
                    binding.pillMyPosts.visibility = if (enabled) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun observeFeed() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NetworkResult.Loading -> {
                            binding.swipeRefresh.isRefreshing = true
                        }
                        is NetworkResult.Success -> {
                            binding.swipeRefresh.isRefreshing = false
                            itemAdapter.submitList(state.data)
                        }
                        is NetworkResult.Error -> {
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}