package com.mmcl.hanapp.ui.discovered

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
import com.mmcl.hanapp.databinding.FragmentDiscoveredBinding
import com.mmcl.hanapp.ui.adapter.GridSpacingItemDecoration
import com.mmcl.hanapp.ui.adapter.ItemAdapter
import com.mmcl.hanapp.ui.feed.FeedViewModel
import com.mmcl.hanapp.ui.feed.FeedViewModelFactory
import com.mmcl.hanapp.ui.home.TabFilterViewModel
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch

// The "Discovered" tab: shows the shared feed of FOUND items pulled live from
// the backend. Supports a "My Posts" mode, toggled by re-tapping the tab in
// HomeFragment, via the shared TabFilterViewModel.
class DiscoveredFragment : Fragment() {

    private var _binding: FragmentDiscoveredBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager

    // FeedViewModel is scoped to this fragment; the factory passes FOUND
    // plus the logged-in user's ID (needed for My Posts filtering).
    private val viewModel: FeedViewModel by viewModels {
        FeedViewModelFactory(PostType.FOUND, SessionManager(requireContext()).getUserId())
    }

    // TabFilterViewModel is scoped to the PARENT fragment (HomeFragment), so
    // this fragment and HomeFragment share the exact same instance — that's
    // what lets a tap on the tab in HomeFragment affect this feed.
    private val tabFilterViewModel: TabFilterViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var itemAdapter: ItemAdapter

    companion object {
        fun newInstance() = DiscoveredFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoveredBinding.inflate(inflater, container, false)
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

        // Tapping the pill itself is an explicit "turn this off" action.
        binding.pillMyPosts.setOnClickListener {
            tabFilterViewModel.clearDiscovered()
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
        binding.recyclerViewDiscovered.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = itemAdapter
            addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingPx = spacingPx))
        }
    }

    // Watches the shared "My Posts" toggle for this tab. When it flips, tells
    // the feed's own ViewModel to re-fetch scoped (or not) to the owner,
    // and shows/hides the pill indicator.
    private fun observeMyPostsFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabFilterViewModel.discoveredMyPostsOnly.collect { enabled ->
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