package com.mmcl.hanapp

import android.app.Application
import com.mmcl.hanapp.data.remote.ApiClient
import com.mmcl.hanapp.data.remote.storage.StorageApiClient

// Runs once when the app process starts, before any Activity.
// Used here to give ApiClient a safe, long-lived context reference
// (applicationContext, not an Activity context, so it can't leak a
// destroyed screen) so it can read the current session's access token.
class HanAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(applicationContext)
        StorageApiClient.init(applicationContext)
    }
}