package com.mmcl.hanapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.mmcl.hanapp.databinding.ActivitySearchBinding
import com.mmcl.hanapp.ui.adapter.GridSpacingItemDecoration
import com.mmcl.hanapp.ui.adapter.ItemAdapter
import com.mmcl.hanapp.ui.search.SearchViewModel
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch

// Global search: matches item name across both FOUND and LOST posts.
// Debounced so typing doesn't fire a request per keystroke.
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchInput()
        observeResults()

        binding.buttonSearchBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter { selectedItem ->
            val intent = ItemDetailActivity.newIntent(this, selectedItem, selectedItem.posterUserId)
            startActivity(intent)
        }

        val spacingPx = (12 * resources.displayMetrics.density).toInt()
        binding.recyclerSearchResults.apply {
            layoutManager = GridLayoutManager(this@SearchActivity, 2)
            adapter = itemAdapter
            addItemDecoration(GridSpacingItemDecoration(spanCount = 2, spacingPx = spacingPx))
        }
    }

    // Feeds every text change into the debounced ViewModel search.
    private fun setupSearchInput() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onQueryChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeResults() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NetworkResult.Loading -> {
                            // No spinner needed for a fast, debounced search;
                            // the empty state text handles the visual gap.
                        }
                        is NetworkResult.Success -> {
                            itemAdapter.submitList(state.data)
                            val hasTypedSomething = binding.editTextSearch.text?.isNotEmpty() == true
                            binding.textSearchEmpty.visibility = when {
                                state.data.isNotEmpty() -> View.GONE
                                !hasTypedSomething -> View.VISIBLE
                                else -> View.VISIBLE
                            }
                            binding.textSearchEmpty.text = if (hasTypedSomething) {
                                getString(R.string.search_empty_no_results)
                            } else {
                                getString(R.string.search_empty_no_query)
                            }
                        }
                        is NetworkResult.Error -> {
                            binding.textSearchEmpty.visibility = View.VISIBLE
                            binding.textSearchEmpty.text = state.message
                        }
                    }
                }
            }
        }
    }
}