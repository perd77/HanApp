package com.mmcl.hanapp.data.remote.auth

import com.mmcl.hanapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Separate Retrofit setup for Supabase's Auth API. Same project and anon key as
// ApiClient, but a different base path (/auth/v1/ instead of /rest/v1/), so it's
// kept as its own client rather than overloading ApiClient's configuration.
object AuthApiClient {

    // Derive the auth base URL from the same Supabase URL used for the data API,
    // swapping "rest/v1" for "auth/v1" so both point at the same project.
    private val baseUrl = BuildConfig.SUPABASE_URL.replace("rest/v1", "auth/v1") + "/"
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Every Supabase Auth call needs the apikey header. Authorization here uses
    // the anon key, since sign-up happens before any user session exists.
    private val authHeaderInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}