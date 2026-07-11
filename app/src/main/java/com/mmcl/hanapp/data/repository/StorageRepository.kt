package com.mmcl.hanapp.data.repository

import android.content.Context
import android.net.Uri
import com.mmcl.hanapp.data.remote.storage.StorageApiClient
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

// Handles uploading a picked photo to Supabase Storage and producing its
// final public URL, which gets saved as an item's photo_path.
class StorageRepository {

    private val api = StorageApiClient.api
    private val bucketName = "item-photos"

    // Reads the picked image's bytes, uploads them under a unique generated
    // filename, and returns the public URL on success.
    suspend fun uploadPhoto(context: Context, photoUri: Uri, userId: String): NetworkResult<String> =
        withContext(Dispatchers.IO) {
            try {
                // Determine the real MIME type (e.g. "image/jpeg") so Supabase
                // stores it correctly and the URL serves with the right type.
                val mimeType = context.contentResolver.getType(photoUri) ?: "image/jpeg"
                val extension = mimeType.substringAfter("/", "jpg")

                // Unique path per upload: userId folder keeps files organized,
                // UUID avoids filename collisions between different users' posts.
                val fileName = "$userId/${UUID.randomUUID()}.$extension"

                // Read the actual image bytes from the picked content URI.
                val bytes = context.contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
                    ?: return@withContext NetworkResult.Error("Could not read the selected photo.")

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val uploadUrl = "${StorageApiClient.projectRootUrl}storage/v1/object/$bucketName/$fileName"

                val response = api.uploadFile(uploadUrl, mimeType, requestBody)

                if (response.isSuccessful) {
                    val publicUrl =
                        "${StorageApiClient.projectRootUrl}storage/v1/object/public/$bucketName/$fileName"
                    NetworkResult.Success(publicUrl)
                } else {
                    NetworkResult.Error("Photo upload failed: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not upload photo. Check your connection.")
            }
        }
}