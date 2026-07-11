package com.mmcl.hanapp.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmcl.hanapp.data.session.SessionManager

// Supplies a SessionManager to NotificationsViewModel's constructor, since
// the default ViewModel factory can't pass constructor arguments.
class NotificationsViewModelFactory(
    private val session: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NotificationsViewModel(session) as T
    }
}