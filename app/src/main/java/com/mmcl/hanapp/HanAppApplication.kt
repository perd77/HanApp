package com.mmcl.hanapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mmcl.hanapp.data.remote.ApiClient
import com.mmcl.hanapp.data.remote.storage.StorageApiClient

class HanAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Force light mode app-wide, regardless of the device's system
        // dark/light setting. HanApp was designed with one single light
        // palette — without this, Android's DayNight theme silently swaps
        // in dark-mode defaults (like white input text) on devices with
        // system dark mode on, which breaks readability since our field
        // backgrounds stay hardcoded light.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        ApiClient.init(applicationContext)
        StorageApiClient.init(applicationContext)
    }
}