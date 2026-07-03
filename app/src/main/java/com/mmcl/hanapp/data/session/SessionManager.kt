package com.mmcl.hanapp.data.session

import android.content.Context
import androidx.core.content.edit

// Stores the current user's name for the whole app session.
// Backed by SharedPreferences so the identity survives app restarts, meaning the user
// isn't forced to re-enter their name every time they open the app.
//
// NOTE: This is a deliberately simplified stand-in for real authentication so the project
// can focus on the lost-and-found claim flow. In production, identity would be verified
// against a server and never trusted from the client alone.
class SessionManager(context: Context) {

    // Private preferences file scoped to this app only.
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Saves the logged-in user's name. Called once on successful login.
    fun setCurrentUser(name: String) {
        prefs.edit { putString(KEY_USERNAME, name) }
    }

    // Returns the current user's name, or null if no one is logged in yet.
    fun getCurrentUser(): String? = prefs.getString(KEY_USERNAME, null)

    // True if someone is currently logged in — used to decide whether to show
    // the login screen or jump straight to the main tabs on app launch.
    fun isLoggedIn(): Boolean = !getCurrentUser().isNullOrBlank()

    // Clears the session. Wired to a future "switch user" / logout action.
    fun logout() {
        prefs.edit { remove(KEY_USERNAME) }
    }

    companion object {
        private const val PREFS_NAME = "hanapp_session"
        private const val KEY_USERNAME = "current_username"
    }
}