package com.mmcl.hanapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// Body sent when submitting a new claim.
data class CreateClaimRequest(
    @SerializedName("item_id") val itemId: Long,
    @SerializedName("claimed_by") val claimedBy: String,
    @SerializedName("contact_number") val contactNumber: String,
    @SerializedName("proof_description") val proofDescription: String,
    @SerializedName("user_id") val userId: String
)

// Mirrors a claim row exactly as Supabase returns it (no joined item data).
data class ClaimDto(
    @SerializedName("id") val id: Long,
    @SerializedName("item_id") val itemId: Long,
    @SerializedName("claimed_by") val claimedBy: String,
    @SerializedName("contact_number") val contactNumber: String,
    @SerializedName("proof_description") val proofDescription: String,
    @SerializedName("claim_status") val claimStatus: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("created_at") val createdAt: String
)

// The minimal item fields we join into a claim query, so the UI can show
// the item's name without a separate request.
data class EmbeddedItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("post_type") val postType: String,
    @SerializedName("user_id") val userId: String? = null
)

// A claim row WITH its parent item embedded — used for the Notifications
// screen, where we need both the claim details and the item's name at once.
data class ClaimWithItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("item_id") val itemId: Long,
    @SerializedName("claimed_by") val claimedBy: String,
    @SerializedName("contact_number") val contactNumber: String,
    @SerializedName("proof_description") val proofDescription: String,
    @SerializedName("claim_status") val claimStatus: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("items") val item: EmbeddedItemDto?
)