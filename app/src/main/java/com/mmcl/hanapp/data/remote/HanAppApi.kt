package com.mmcl.hanapp.data.remote

import com.mmcl.hanapp.data.remote.dto.ClaimDto
import com.mmcl.hanapp.data.remote.dto.ClaimWithItemDto
import com.mmcl.hanapp.data.remote.dto.CreateClaimRequest
import com.mmcl.hanapp.data.remote.dto.CreateItemRequest
import com.mmcl.hanapp.data.remote.dto.ItemDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

// Endpoints against Supabase's auto-generated REST API (PostgREST).
interface HanAppApi {

    @GET("items")
    suspend fun getItems(
        @Query("post_type") postType: String,
        @Query("user_id") userId: String? = null,
        @Query("status") status: String? = null,
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): Response<List<ItemDto>>

    // Searches by item name across BOTH post types at once (no post_type
    // filter), matching only still-available items. "name" uses PostgREST's
    // ilike operator for case-insensitive partial matching, passed in fully
    // formed (e.g. "ilike.*backpack*") from the repository.
    @GET("items")
    suspend fun searchItems(
        @Query("name") nameFilter: String,
        @Query("status") status: String = "eq.UNCLAIMED",
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): Response<List<ItemDto>>

    @POST("items")
    suspend fun createItem(
        @Body request: CreateItemRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<ItemDto>>

    @POST("claims")
    suspend fun createClaim(
        @Body request: CreateClaimRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<ClaimDto>>

    @GET("claims")
    suspend fun getIncomingClaims(
        @Query("select") select: String = "*,items!inner(id,name,post_type,user_id)",
        @Query("items.user_id") ownerFilter: String,
        @Query("order") order: String = "created_at.desc"
    ): Response<List<ClaimWithItemDto>>

    @GET("claims")
    suspend fun getOutgoingClaims(
        @Query("select") select: String = "*,items(id,name,post_type)",
        @Query("user_id") claimantFilter: String,
        @Query("order") order: String = "created_at.desc"
    ): Response<List<ClaimWithItemDto>>

    @POST("rpc/approve_claim")
    suspend fun approveClaim(@Body body: Map<String, Long>): Response<Unit>

    @PATCH("claims")
    suspend fun rejectClaim(
        @Query("id") claimIdFilter: String,
        @Body body: Map<String, String>
    ): Response<Unit>
}