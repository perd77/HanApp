package com.mmcl.hanapp.data.remote

import android.content.Context
import com.mmcl.hanapp.BuildConfig
import com.mmcl.hanapp.data.session.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Single shared Retrofit setup for Supabase's data REST API.
// Unlike the earlier version, this now attaches the CURRENT logged-in user's
// access token to every request (read fresh each time, not baked in once),
// which is what lets Supabase's RLS policies check auth.uid() correctly.
object ApiClient {

    private val baseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    // Set once at app startup via HanAppApplication. Using applicationContext
    // (not an Activity) means this can't leak a destroyed screen.
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Built fresh each time it's accessed rather than cached, so it always
    // reflects whichever session is currently active (or none, if logged out).
    private val sessionManager: SessionManager
        get() = SessionManager(appContext)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Attaches Supabase's required headers to every outgoing request:
    //  - apikey: always the anon key (identifies the project itself)
    //  - Authorization: the LOGGED-IN USER'S token if available, so RLS
    //    policies can evaluate auth.uid() against the real account. Falls
    //    back to the anon key only if somehow no session exists yet.
    private val supabaseHeaderInterceptor = okhttp3.Interceptor { chain ->
        val userToken = sessionManager.getAccessToken()
        val authToken = userToken ?: anonKey

        val request = chain.request().newBuilder()
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $authToken")
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(supabaseHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: HanAppApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HanAppApi::class.java)
    }
}