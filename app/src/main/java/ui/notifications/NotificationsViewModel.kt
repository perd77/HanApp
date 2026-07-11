package com.mmcl.hanapp.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmcl.hanapp.data.repository.ItemRepository
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.ui.model.NotificationClaim
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Holds both claim lists for the Notifications screen: claims on the user's
// own posts (actionable) and claims the user submitted elsewhere (read-only).
class NotificationsViewModel(private val session: SessionManager) : ViewModel() {

    private val repository = ItemRepository()

    private val _incomingState =
        MutableStateFlow<NetworkResult<List<NotificationClaim>>>(NetworkResult.Loading)
    val incomingState: StateFlow<NetworkResult<List<NotificationClaim>>> = _incomingState.asStateFlow()

    private val _outgoingState =
        MutableStateFlow<NetworkResult<List<NotificationClaim>>>(NetworkResult.Loading)
    val outgoingState: StateFlow<NetworkResult<List<NotificationClaim>>> = _outgoingState.asStateFlow()

    init {
        loadAll()
    }

    // Reloads both lists — called on first load and after any approve/reject
    // action, so the screen always reflects the latest state.
    fun loadAll() {
        val userId = session.getUserId() ?: return
        viewModelScope.launch {
            _incomingState.value = NetworkResult.Loading
            _incomingState.value = repository.getIncomingClaims(userId)
        }
        viewModelScope.launch {
            _outgoingState.value = NetworkResult.Loading
            _outgoingState.value = repository.getOutgoingClaims(userId)
        }
    }

    // Approves a claim, then refreshes both lists so the change is reflected
    // immediately (the approved claim, the auto-rejected others, and the
    // item's new CLAIMED status all ripple through on reload).
    fun approveClaim(claimId: Long, onResult: (NetworkResult<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.approveClaim(claimId)
            onResult(result)
            if (result is NetworkResult.Success) loadAll()
        }
    }

    // Rejects a single claim, then refreshes.
    fun rejectClaim(claimId: Long, onResult: (NetworkResult<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = repository.rejectClaim(claimId)
            onResult(result)
            if (result is NetworkResult.Success) loadAll()
        }
    }
}