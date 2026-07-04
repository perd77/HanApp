package com.mmcl.hanapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmcl.hanapp.data.repository.ItemRepository
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Holds and manages one feed's data. The post type (FOUND/LOST) is set once
// when the ViewModel is created, so the same class serves both tabs.
class FeedViewModel(private val postType: PostType) : ViewModel() {

    private val repository = ItemRepository()

    // Internal mutable state; only the ViewModel changes it.
    private val _uiState = MutableStateFlow<NetworkResult<List<Item>>>(NetworkResult.Loading)
    // Public read-only state the fragment observes.
    val uiState: StateFlow<NetworkResult<List<Item>>> = _uiState.asStateFlow()

    // Load this feed's items as soon as the ViewModel is created.
    init {
        loadItems()
    }

    // Reloads items for this feed's assigned post type.
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = NetworkResult.Loading
            _uiState.value = repository.getItems(postType)
        }
    }
}