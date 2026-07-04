package com.mmcl.hanapp.data.repository

import com.mmcl.hanapp.data.remote.ApiClient
import com.mmcl.hanapp.data.remote.dto.CreateItemRequest
import com.mmcl.hanapp.data.remote.dto.toDomain
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Single source of truth for item data, talking to Supabase's REST API.
class ItemRepository {

    private val api = ApiClient.api

    // Fetches a feed filtered by post type (FOUND for Discovered, LOST for Finding).
    // Supabase returns a bare JSON array of items.
    suspend fun getItems(postType: PostType): NetworkResult<List<Item>> =
        withContext(Dispatchers.IO) {
            try {
                // PostgREST equality filter, e.g. post_type=eq.FOUND
                val filter = "eq.${postType.name}"
                val response = api.getItems(postType = filter)
                if (response.isSuccessful && response.body() != null) {
                    // Convert each raw network DTO into a strict domain Item.
                    val items = response.body()!!.map { it.toDomain() }
                    NetworkResult.Success(items)
                } else {
                    NetworkResult.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    // Posts a new item; Supabase returns the created row, so we read its id.
    suspend fun createItem(request: CreateItemRequest): NetworkResult<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createItem(request)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    // Response is a one-element array holding the newly created row.
                    NetworkResult.Success(response.body()!!.first().id)
                } else {
                    NetworkResult.Error("Failed to post item: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }
}