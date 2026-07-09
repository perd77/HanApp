package com.mmcl.hanapp.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// Supabase's built-in Auth service (GoTrue). Separate from the data API —
// it lives at /auth/v1/ instead of /rest/v1/.
interface AuthApi {

    // Creates a new account. Since email confirmation is disabled, this
    // returns a usable session immediately — the user is logged in right away.
    @POST("signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthSessionResponse>

    // Logs an existing user in. grant_type=password tells Supabase this is a
// standard email+password login (as opposed to refresh-token or OAuth flows).
    @POST("token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body request: LoginRequest
    ): Response<AuthSessionResponse>
}

