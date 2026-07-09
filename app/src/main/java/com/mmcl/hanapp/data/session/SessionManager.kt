package com.mmcl.hanapp.data.session

import android.content.Context
import androidx.core.content.edit

// Stores the current authenticated session: a real access token issued by
// Supabase, the refresh token, the account's user ID, and the display username.
// This replaces the earlier name-only session with a genuinely verified identity.
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Saves a full session after a successful sign-up or login.
    fun saveSession(accessToken: String, refreshToken: String, userId: String, username: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
        }
    }

    // The token that must be attached to every authenticated API request
    // (used starting in the next batch, when we wire it into ApiClient).
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // The real, database-enforceable identity — this is what RLS policies
    // compare against via auth.uid().
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    // The display name shown in the UI (e.g. on posted items).
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    // True if there's a real, saved access token — i.e. a genuine active session.
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    // Clears the entire session on logout.
    fun logout() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "hanapp_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }
}