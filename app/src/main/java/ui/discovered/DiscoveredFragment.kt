package com.mmcl.hanapp.ui.discovered

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mmcl.hanapp.databinding.FragmentDiscoveredBinding
import com.mmcl.hanapp.ui.adapter.ItemAdapter
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.ItemCategory
import com.mmcl.hanapp.ui.model.ItemStatus
import com.mmcl.hanapp.ui.adapter.GridSpacingItemDecoration

// The "Discovered" tab — shows found items posted by Discoverers, newest first.
class DiscoveredFragment : Fragment() {

    // Nullable binding + non-null getter is the standard safe pattern for Fragments:
    // the view can be destroyed (e.g. tab swiped away) while the Fragment itself stays alive,
    // so we clear the reference in onDestroyView to avoid holding a dead view in memory.
    private var _binding: FragmentDiscoveredBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: ItemAdapter

    companion object {
        // Standard factory method pattern for creating this fragment — keeps construction
        // consistent and leaves room to pass arguments later (e.g. a filter) without
        // changing the constructor itself.
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

        // onItemClick is currently a no-op placeholder until the item detail screen exists.
        itemAdapter = ItemAdapter { selectedItem ->
            // TODO: navigate to item detail screen once it exists
        }

        binding.recyclerViewDiscovered.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = itemAdapter

            // 12dp gap converted to pixels so it scales correctly across screen densities.
            val spacingPx = (12 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingPx = spacingPx))
        }

        // Temporary local sample data so the feed has something to display and scroll.
        // Replace with a real GET /items API call once the networking layer is built.
        itemAdapter.submitList(sampleDiscoveredItems())

        binding.swipeRefresh.setOnRefreshListener {
            // TODO: trigger GET /items refresh once networking layer exists
            binding.swipeRefresh.isRefreshing = false
        }
    }

    // Hardcoded placeholder items — stand-in for real backend data during UI development.
    private fun sampleDiscoveredItems(): List<Item> = listOf(
        Item(
            id = "1",
            name = "Blue Backpack",
            description = "Found near the library entrance, has a small keychain attached.",
            category = ItemCategory.BAG,
            locationTag = "Library",
            status = ItemStatus.UNCLAIMED,
            timestampLabel = "2h ago"
        ),
        Item(
            id = "2",
            name = "Student ID Card",
            description = "Found on a table at the canteen during lunch hours.",
            category = ItemCategory.ID_CARDS,
            locationTag = "Canteen",
            status = ItemStatus.UNCLAIMED,
            timestampLabel = "5h ago"
        ),
        Item(
            id = "3",
            name = "Wired Earphones",
            description = "White earphones left on a chair in Building A.",
            category = ItemCategory.ELECTRONICS,
            locationTag = "Building A",
            status = ItemStatus.CLAIMED,
            timestampLabel = "1d ago"
        )
    )

    // Prevents a memory leak: without this, the binding would keep referencing views
    // that no longer exist once the Fragment's view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}