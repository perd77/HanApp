package com.mmcl.hanapp.data.remote.storage

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

// Supabase's Storage REST API. Unlike the data/auth APIs, the upload path is
// dynamic (includes the bucket name and generated filename), so @Url is used
// instead of a fixed endpoint string.
interface StorageApi {

    // Uploads raw file bytes to the given path. Content-Type must match the
    // actual file type (e.g. "image/jpeg") so Supabase stores it correctly.
    @POST
    suspend fun uploadFile(
        @Url uploadUrl: String,
        @Header("Content-Type") contentType: String,
        @Body file: RequestBody
    ): Response<Unit>
}