package com.mmcl.hanapp.ui.model

// The three states a claim can be in.
enum class ClaimApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

// A claim shown in the Notifications screen, with the item's name and post
// type already attached (fetched via a joined query) so the UI can decide
// wording ("Approve/Reject" vs "This Is Mine/Not Mine") without a second lookup.
data class NotificationClaim(
    val claimId: Long,
    val itemId: Long,
    val itemName: String,
    val itemPostType: PostType,
    val claimantName: String,
    val contactNumber: String,
    val proofDescription: String,
    val status: ClaimApprovalStatus,
    val createdAtLabel: String
)