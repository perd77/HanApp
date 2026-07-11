package com.mmcl.hanapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// Mirrors one item object exactly as the PHP GET /items endpoint returns it.
// @SerializedName maps the server's snake_case JSON keys to Kotlin camelCase properties,
// so the JSON field "location_tag" fills the Kotlin property "locationTag" automatically.
data class ItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("location_tag") val locationTag: String,
    @SerializedName("post_type") val postType: String,
    @SerializedName("status") val status: String,
    @SerializedName("posted_by") val postedBy: String,
    @SerializedName("photo_path") val photoPath: String?,
    @SerializedName("user_id") val userId: String?,// nullable: item may have no photo
    @SerializedName("created_at") val createdAt: String

)

// Wraps the whole GET /items response: { "success": true, "items": [ ... ] }.
// Gson needs a class that matches this outer envelope, not just the inner array.
data class ItemListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("items") val items: List<ItemDto>
)

// Body we SEND when posting a new item to POST /items.
// Field names here are what Gson writes into the outgoing JSON, so they must match
// exactly what the PHP endpoint reads ($input["location_tag"], etc.).
// Body we SEND when posting a new item to POST /items.
// user_id must match the logged-in user's real ID — RLS enforces this,
// so a mismatched or missing user_id causes Supabase to reject the insert.
data class CreateItemRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("location_tag") val locationTag: String,
    @SerializedName("posted_by") val postedBy: String,
    @SerializedName("post_type") val postType: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("photo_path") val photoPath: String? = null
)

// The response from POST /items: { "success": true, "id": 5, "message": "..." }.
data class CreateItemResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("message") val message: String
)

