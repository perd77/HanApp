package com.mmcl.hanapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmcl.hanapp.ui.model.PostType

// Supplies a PostType to FeedViewModel's constructor.
// The default ViewModel factory can't pass constructor arguments, so this
// small factory does it — it's what lets each tab tell its ViewModel whether
// to load FOUND items (Discovered) or LOST items (Finding).
class FeedViewModelFactory(private val postType: PostType) : ViewModelProvider.Factory {

    // Called by the system when it needs a FeedViewModel; we build one and
    // hand it the postType this factory was created with.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel(postType) as T
    }
}