package com.mmcl.hanapp.data.remote.dto

import com.mmcl.hanapp.ui.model.ClaimApprovalStatus
import com.mmcl.hanapp.ui.model.NotificationClaim
import com.mmcl.hanapp.ui.model.PostType
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Converts a claim-with-embedded-item response into the clean NotificationClaim
// the UI actually displays.
fun ClaimWithItemDto.toNotificationClaim(): NotificationClaim {
    return NotificationClaim(
        claimId = id,
        itemId = itemId,
        itemName = item?.name ?: "Unknown item",
        // Determines whether this claim is on a FOUND item or a LOST post,
        // which drives the Approve/Reject vs This Is Mine/Not Mine wording.
        itemPostType = when (item?.postType?.uppercase()) {
            "LOST" -> PostType.LOST
            else -> PostType.FOUND
        },
        claimantName = claimedBy,
        contactNumber = contactNumber,
        proofDescription = proofDescription,
        status = when (claimStatus.uppercase()) {
            "APPROVED" -> ClaimApprovalStatus.APPROVED
            "REJECTED" -> ClaimApprovalStatus.REJECTED
            else -> ClaimApprovalStatus.PENDING
        },
        createdAtLabel = formatClaimTimestamp(createdAt)
    )
}

private fun formatClaimTimestamp(raw: String): String {
    return try {
        val postedTime = OffsetDateTime.parse(raw).toInstant()
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(postedTime, now)
        val hours = ChronoUnit.HOURS.between(postedTime, now)
        val days = ChronoUnit.DAYS.between(postedTime, now)
        when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> postedTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
        }
    } catch (e: Exception) {
        raw.substringBefore("T").ifBlank { raw }
    }
}