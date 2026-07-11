package com.mmcl.hanapp.data.repository

import com.mmcl.hanapp.data.remote.ApiClient
import com.mmcl.hanapp.data.remote.dto.CreateClaimRequest
import com.mmcl.hanapp.data.remote.dto.CreateItemRequest
import com.mmcl.hanapp.data.remote.dto.toDomain
import com.mmcl.hanapp.data.remote.dto.toNotificationClaim
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.NotificationClaim
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository {

    private val api = ApiClient.api

    // Fetches a feed filtered by post type. If ownerUserId is provided (My
    // Posts mode), shows ALL of that user's posts regardless of status —
    // so they can see their own resolved items too. Otherwise, the normal
    // browse view explicitly excludes CLAIMED items, since a resolved item
    // has nothing left to browse.
    suspend fun getItems(postType: PostType, ownerUserId: String? = null): NetworkResult<List<Item>> =
        withContext(Dispatchers.IO) {
            try {
                val typeFilter = "eq.${postType.name}"
                val ownerFilter = ownerUserId?.let { "eq.$it" }
                // Only exclude CLAIMED items in the normal (non-My-Posts) view.
                val statusFilter = if (ownerUserId == null) "eq.UNCLAIMED" else null

                val response = api.getItems(
                    postType = typeFilter,
                    userId = ownerFilter,
                    status = statusFilter
                )
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!.map { it.toDomain() })
                } else {
                    NetworkResult.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

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

    suspend fun createClaim(request: CreateClaimRequest): NetworkResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createClaim(request)
                if (response.isSuccessful) NetworkResult.Success(Unit)
                else NetworkResult.Error("Failed to submit claim: ${response.code()}")
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    suspend fun getIncomingClaims(userId: String): NetworkResult<List<NotificationClaim>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getIncomingClaims(ownerFilter = "eq.$userId")
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!.map { it.toNotificationClaim() })
                } else {
                    NetworkResult.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

    suspend fun getOutgoingClaims(userId: String): NetworkResult<List<NotificationClaim>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getOutgoingClaims(claimantFilter = "eq.$userId")
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!.map { it.toNotificationClaim() })
                } else {
                    NetworkResult.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Could not reach the server. Is it running?")
            }
        }

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