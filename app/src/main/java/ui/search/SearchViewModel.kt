package com.mmcl.hanapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmcl.hanapp.data.repository.ItemRepository
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Holds search results and debounces the query, so a fast typist doesn't
// fire a network request on every single keystroke.
class SearchViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _uiState = MutableStateFlow<NetworkResult<List<Item>>>(NetworkResult.Success(emptyList()))
    val uiState: StateFlow<NetworkResult<List<Item>>> = _uiState.asStateFlow()

    // Tracks the in-flight debounce, so a new keystroke cancels the pending
    // search from the previous one rather than letting both fire.
    private var searchJob: Job? = null

    // Called on every text change. Waits briefly for typing to pause before
    // actually hitting the network — the standard "search as you type"
    // pattern without spamming requests on every character.
    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce window
            _uiState.value = NetworkResult.Loading
            _uiState.value = repository.searchItems(query)
        }
    }
}