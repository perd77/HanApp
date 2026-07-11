package com.mmcl.hanapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmcl.hanapp.ui.model.PostType

// Supplies a PostType and the logged-in user's ID to FeedViewModel's
// constructor, since ViewModels can't take constructor args directly.
class FeedViewModelFactory(
    private val postType: PostType,
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel(postType, userId) as T
    }
}