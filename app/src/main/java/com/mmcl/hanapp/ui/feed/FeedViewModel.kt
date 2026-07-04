package com.mmcl.hanapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmcl.hanapp.data.repository.ItemRepository
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Holds and manages the feed's data and loading/error state.
// Survives configuration changes (rotation, tab swaps), so data isn't re-fetched
// or lost when the fragment's view is recreated.
class FeedViewModel : ViewModel() {

    private val repository = ItemRepository()

    // Internal mutable state; only the ViewModel can change it.
    private val _uiState = MutableStateFlow<NetworkResult<List<Item>>>(NetworkResult.Loading)
    // Public read-only view the fragment observes. This one-way exposure prevents
    // the UI from accidentally mutating state it shouldn't own.
    val uiState: StateFlow<NetworkResult<List<Item>>> = _uiState.asStateFlow()

    // Kick off an initial load as soon as the ViewModel is created.
    init {
        loadItems()
    }

    // Fetches items and pushes each state (loading → success/error) to the UI.
    // viewModelScope auto-cancels this coroutine if the ViewModel is destroyed,
    // preventing leaks and crashes from updating a gone screen.
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = NetworkResult.Loading
            _uiState.value = repository.getItems()   // no arguments now
        }
    }
}