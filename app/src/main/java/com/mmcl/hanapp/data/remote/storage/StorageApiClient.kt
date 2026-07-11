package com.mmcl.hanapp.data.remote.storage

import com.mmcl.hanapp.BuildConfig
import com.mmcl.hanapp.data.session.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Separate Retrofit client for Supabase Storage uploads. Storage's upload
// policy requires an authenticated user, so every request carries the
// logged-in user's real access token (read fresh, same pattern as ApiClient).
object StorageApiClient {

    // The project's root URL, derived by stripping the data API's "rest/v1/"
    // suffix — Storage lives at a different path on the same project.
    val projectRootUrl: String = BuildConfig.SUPABASE_URL.replace("rest/v1/", "")

    private val anonKey = BuildConfig.SUPABASE_ANON_KEY
    private lateinit var appContext: android.content.Context

    fun init(context: android.content.Context) {
        appContext = context.applicationContext
    }

    private val sessionManager: SessionManager
        get() = SessionManager(appContext)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Attaches the current user's real token — required because the
    // "authenticated users can upload" Storage policy checks this.
    private val storageHeaderInterceptor = okhttp3.Interceptor { chain ->
        val userToken = sessionManager.getAccessToken() ?: anonKey
        val request = chain.request().newBuilder()
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $userToken")
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(storageHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: StorageApi by lazy {
        Retrofit.Builder()
            .baseUrl(projectRootUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StorageApi::class.java)
    }
}