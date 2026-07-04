package com.mmcl.hanapp.data.remote

import com.mmcl.hanapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Single shared Retrofit setup, now pointed at Supabase's auto-generated REST API.
object ApiClient {

    // Base URL of the Supabase REST API, pulled from local.properties via BuildConfig
    // (never hardcoded, so the anon key/URL aren't committed to the public repo).
    private val baseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    // Logs full request/response bodies to Logcat during development.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Adds the two headers Supabase requires on every request:
    //  - apikey: identifies the project
    //  - Authorization: Bearer <anon key> sets the 'anon' role that RLS policies allow
    // Centralizing this here means individual API calls never repeat auth boilerplate.
    private val supabaseHeaderInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
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