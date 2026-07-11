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

// Holds and manages one feed's data. postType is fixed at creation (FOUND or
// LOST). userId is the logged-in user's real ID, used only when "My Posts"
// mode is toggled on, to scope the feed to just their own posts.
class FeedViewModel(
    private val postType: PostType,
    private val userId: String?
) : ViewModel() {

    private val repository = ItemRepository()

    private val _uiState = MutableStateFlow<NetworkResult<List<Item>>>(NetworkResult.Loading)
    val uiState: StateFlow<NetworkResult<List<Item>>> = _uiState.asStateFlow()

    // Tracks whether this feed is currently scoped to only the user's own posts.
    private var myPostsOnly = false

    init {
        loadItems()
    }

    // Reloads items, respecting the current "My Posts" toggle state.
    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = NetworkResult.Loading
            val ownerFilter = if (myPostsOnly) userId else null
            _uiState.value = repository.getItems(postType, ownerFilter)
        }
    }

    // Switches "My Posts" mode on/off and reloads accordingly.
    fun setMyPostsOnly(enabled: Boolean) {
        if (myPostsOnly == enabled) return
        myPostsOnly = enabled
        loadItems()
    }
}