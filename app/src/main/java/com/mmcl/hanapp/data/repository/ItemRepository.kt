package com.mmcl.hanapp.data.repository

import com.mmcl.hanapp.data.remote.ApiClient
import com.mmcl.hanapp.data.remote.dto.CreateItemRequest
import com.mmcl.hanapp.data.remote.dto.toDomain
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mmcl.hanapp.data.remote.dto.CreateClaimRequest

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
// userId must be the real logged-in account's ID (from SessionManager),
// since RLS requires it to match auth.uid() for the insert to succeed.
    suspend fun createItem(request: CreateItemRequest): NetworkResult<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createItem(request)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    NetworkResult.Success(response.body()!!.first().id)
                } else {
                    NetworkResult.Error("Failed to post item: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    // Submits a claim (or "I have this item" report) on an item.
    suspend fun createClaim(request: CreateClaimRequest): NetworkResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createClaim(request)
                if (response.isSuccessful) {
                    NetworkResult.Success(Unit)
                } else {
                    NetworkResult.Error("Failed to submit claim: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    // Approves a claim atomically (see approve_claim RPC in HanAppApi).
    suspend fun approveClaim(claimId: Long): NetworkResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.approveClaim(mapOf("claim_id_input" to claimId))
                if (response.isSuccessful) NetworkResult.Success(Unit)
                else NetworkResult.Error("Failed to approve claim: ${response.code()}")
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    // Rejects a single claim directly.
    suspend fun rejectClaim(claimId: Long): NetworkResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.rejectClaim("eq.$claimId", mapOf("claim_status" to "REJECTED"))
                if (response.isSuccessful) NetworkResult.Success(Unit)
                else NetworkResult.Error("Failed to reject claim: ${response.code()}")
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }
}