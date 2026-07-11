package com.mmcl.hanapp.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Shared between HomeFragment (which owns the tab strip) and its two child
// feed fragments. Lets a re-tap on the active tab toggle a "my posts only"
// filter inside whichever feed is currently showing, without the tabs and
// feeds needing any other direct connection to each other.
class TabFilterViewModel : ViewModel() {

    private val _discoveredMyPostsOnly = MutableStateFlow(false)
    val discoveredMyPostsOnly: StateFlow<Boolean> = _discoveredMyPostsOnly.asStateFlow()

    private val _findingMyPostsOnly = MutableStateFlow(false)
    val findingMyPostsOnly: StateFlow<Boolean> = _findingMyPostsOnly.asStateFlow()

    fun toggleDiscovered() {
        _discoveredMyPostsOnly.value = !_discoveredMyPostsOnly.value
    }

    fun toggleFinding() {
        _findingMyPostsOnly.value = !_findingMyPostsOnly.value
    }

    // Explicit "turn off" for when the pill itself is tapped, rather than
    // the tab — always sets to false regardless of current state.
    fun clearDiscovered() {
        _discoveredMyPostsOnly.value = false
    }

    fun clearFinding() {
        _findingMyPostsOnly.value = false
    }
}