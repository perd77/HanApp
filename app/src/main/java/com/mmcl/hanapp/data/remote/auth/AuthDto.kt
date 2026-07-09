package com.mmcl.hanapp.data.remote.auth

import com.google.gson.annotations.SerializedName

// What we send to Supabase's sign-up endpoint. Supabase requires an "email" field
// even though the user only typed a username — we generate a fake one behind the
// scenes (see AuthRepository). user_metadata lets us also store their real username.
data class SignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("data") val data: SignUpMetadata
)

// Extra profile data attached to the account. Storing the real username here means
// we can display it later even though the "email" itself is a fabricated placeholder.
data class SignUpMetadata(
    @SerializedName("username") val username: String
)

// Supabase's response after a successful sign-up (since email confirmation is off,
// this includes a ready-to-use session immediately).
data class AuthSessionResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("user") val user: SupabaseUser
)

// The account details Supabase returns alongside the session.
data class SupabaseUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("user_metadata") val userMetadata: SignUpMetadata?
)

// What we send to Supabase's login endpoint.
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)