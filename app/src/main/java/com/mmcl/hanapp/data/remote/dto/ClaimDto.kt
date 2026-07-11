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

// Mirrors a claim row exactly as Supabase returns it.
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