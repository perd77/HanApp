package com.mmcl.hanapp.data.remote.dto

import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.ItemCategory
import com.mmcl.hanapp.ui.model.ItemStatus
import com.mmcl.hanapp.ui.model.PostType
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Converts a raw network ItemDto into the app's strict domain Item.
// Keeping this conversion in one place means if the API shape changes, only this
// file is touched — the rest of the app keeps using the clean Item model.
fun ItemDto.toDomain(): Item {
    return Item(
        id = id.toString(),
        name = name,
        description = description,
        // Match the server's category string to our enum; fall back to OTHERS
        // if the server ever sends something unexpected, so the UI never crashes.
        category = ItemCategory.entries.firstOrNull { it.displayName == category }
            ?: ItemCategory.OTHERS,
        locationTag = locationTag,
        // Server sends "UNCLAIMED"/"CLAIMED"; map to the enum, defaulting safely.
        status = when (status.uppercase()) {
            "CLAIMED" -> ItemStatus.CLAIMED
            else -> ItemStatus.UNCLAIMED
        },
        // Server sends "FOUND"/"LOST"; map to the enum, defaulting to FOUND
        // so an unexpected value never crashes the feed.
        postType = when (postType.uppercase()) {
            "LOST" -> PostType.LOST
            else -> PostType.FOUND
        },
        timestampLabel = formatTimestamp(createdAt),
        photoUrl = photoPath
    )
}

// Converts a Supabase ISO-8601 timestamp into a short, human-friendly label.
private fun formatTimestamp(raw: String): String {
    return try {
        val postedTime = OffsetDateTime.parse(raw).toInstant()
        val now = Instant.now()

        val seconds = ChronoUnit.SECONDS.between(postedTime, now)
        val minutes = ChronoUnit.MINUTES.between(postedTime, now)
        val hours = ChronoUnit.HOURS.between(postedTime, now)
        val days = ChronoUnit.DAYS.between(postedTime, now)

        when {
            seconds < 5 -> "just now"
            seconds < 60 -> "${seconds}s ago"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> {
                val localDate = postedTime.atZone(ZoneId.systemDefault())
                localDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
            }
        }
    } catch (e: Exception) {
        raw.substringBefore("T").ifBlank { raw }
    }
}