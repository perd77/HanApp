package com.mmcl.hanapp.data.repository

import com.mmcl.hanapp.data.remote.auth.AuthApiClient
import com.mmcl.hanapp.data.remote.auth.AuthSessionResponse
import com.mmcl.hanapp.data.remote.auth.SignUpMetadata
import com.mmcl.hanapp.data.remote.auth.SignUpRequest
import com.mmcl.hanapp.data.remote.auth.LoginRequest
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Handles account creation against Supabase Auth.
// Supabase requires a real email format, but our app only wants a username —
// so we generate a placeholder email from the username. The real username is
// still stored via user_metadata so it can be shown in the UI.
class AuthRepository {

    private val api = AuthApiClient.api

    // A fixed, fake domain so every account gets a valid-looking but non-real email.
    private val fakeEmailDomain = "@hanapp.local"

    suspend fun signUp(username: String, password: String): NetworkResult<AuthSessionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = SignUpRequest(
                    email = "$username$fakeEmailDomain",
                    password = password,
                    data = SignUpMetadata(username = username)
                )
                val response = api.signUp(request)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    // Supabase returns 422/400 for things like "already registered"
                    // or a password under 6 characters.
                    NetworkResult.Error(mapSignUpError(response.code()))
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }
    suspend fun login(username: String, password: String): NetworkResult<AuthSessionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(
                    email = "$username$fakeEmailDomain",
                    password = password
                )
                val response = api.login(request = request)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    NetworkResult.Error(mapLoginError(response.code()))
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    private fun mapLoginError(code: Int): String = when (code) {
        400, 422 -> "Incorrect username or password."
        else -> "Login failed: server error ($code)"
    }

    // Translates common Supabase error codes into messages that make sense
    // for a username-based sign-up, since the raw error mentions "email".
    private fun mapSignUpError(code: Int): String = when (code) {
        422, 400 -> "That username is taken, or the password is too short (min 6 characters)."
        else -> "Sign-up failed: server error ($code)"
    }
}