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
import com.mmcl.hanapp.databinding.FragmentFindingBinding
import com.mmcl.hanapp.ui.adapter.GridSpacingItemDecoration
import com.mmcl.hanapp.ui.adapter.ItemAdapter
import com.mmcl.hanapp.ui.feed.FeedViewModel
import com.mmcl.hanapp.ui.feed.FeedViewModelFactory
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch
import com.mmcl.hanapp.ItemDetailActivity

// The "Finding" tab: shows LOST-item posts ("I lost this, looking for it"),
// pulled live from the backend. Mirrors DiscoveredFragment but filters for LOST.
class FindingFragment : Fragment() {

    private var _binding: FragmentFindingBinding? = null
    private val binding get() = _binding!!

    // ViewModel scoped to this fragment. The factory passes PostType.LOST so this
    // feed only loads lost-item posts.
    private val viewModel: FeedViewModel by viewModels {
        FeedViewModelFactory(PostType.LOST)
    }

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFeed()

        // Pull-to-refresh re-requests the LOST feed from the server.
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadItems()
        }
    }

    // Same 2-column grid + spacing as the Discovered feed, for visual consistency.
    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            // Opens the full detail screen, passing the item's data and its
            // real poster ID so the detail screen can decide whether to show
            // the Claim button or the "you posted this" notice.
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

    // Observes the ViewModel's state and updates the UI for each case.
    // repeatOnLifecycle collects only while the view is at least STARTED,
    // pausing automatically when backgrounded — no wasted work, no leaks.
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