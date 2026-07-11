package com.mmcl.hanapp.data.remote

import com.mmcl.hanapp.data.remote.dto.CreateItemRequest
import com.mmcl.hanapp.data.remote.dto.ItemDto
import com.mmcl.hanapp.data.remote.dto.ClaimDto
import com.mmcl.hanapp.data.remote.dto.CreateClaimRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// Endpoints against Supabase's auto-generated REST API (PostgREST).
// The table name is the path; filtering and sorting are done with query params.
interface HanAppApi {

    // GET a feed filtered by post type (FOUND for Discovered, LOST for Finding),
    // newest first, all columns.
    // PostgREST filter syntax: "eq.FOUND" means "equals FOUND".
    // "created_at.desc" sorts newest first; "*" selects all columns.
    @GET("items")
    suspend fun getItems(
        @Query("post_type") postType: String,
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): Response<List<ItemDto>>

    // POST a new item. The Prefer header tells Supabase to return the created row,
    // so we get the new id back instead of an empty response.
    @POST("items")
    suspend fun createItem(
        @Body request: CreateItemRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<ItemDto>>

    // Submits a new claim on an item.
    @POST("claims")
    suspend fun createClaim(
        @Body request: CreateClaimRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<ClaimDto>>

    // Approves a claim atomically via the approve_claim RPC function — this
// marks the claim APPROVED, the item CLAIMED, and rejects other pending
// claims on the same item, all as one database transaction.
    @POST("rpc/approve_claim")
    suspend fun approveClaim(@Body body: Map<String, Long>): Response<Unit>

    // Directly rejects a single claim (RLS only permits the item's owner to
// set REJECTED this way — approval must go through the RPC above).
    @retrofit2.http.PATCH("claims")
    suspend fun rejectClaim(
        @Query("id") claimIdFilter: String, // e.g. "eq.5"
        @Body body: Map<String, String>
    ): Response<Unit>
}