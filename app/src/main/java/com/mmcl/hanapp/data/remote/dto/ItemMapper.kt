package com.mmcl.hanapp.data.remote.dto

import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.ItemCategory
import com.mmcl.hanapp.ui.model.ItemStatus
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
// Converts a raw network ItemDto into the app's strict domain Item.
// Keeping this conversion in one place means if the API shape changes, only this file
// is touched — the rest of the app keeps using the clean Item model.
fun ItemDto.toDomain(): Item {
    return Item(
        id = id.toString(),
        name = name,
        description = description,
        // Match the server's category string back to our enum. Falls back to OTHERS
        // if the server ever sends something unexpected, so the UI never crashes.
        category = ItemCategory.entries.firstOrNull { it.displayName == category }
            ?: ItemCategory.OTHERS,
        locationTag = locationTag,
        // Server sends "UNCLAIMED"/"CLAIMED"; map to the enum, defaulting safely.
        status = when (status.uppercase()) {
            "CLAIMED" -> ItemStatus.CLAIMED
            else -> ItemStatus.UNCLAIMED
        },
        // Convert the server timestamp into a short relative label for the card.
        timestampLabel = formatTimestamp(createdAt),
        photoUrl = photoPath
    )
}

// Turns a MySQL datetime string (e.g. "2026-01-15 14:30:00") into a simple label.
// Kept intentionally lightweight for now; can be upgraded to true "2h ago" logic later.
private fun formatTimestamp(raw: String): String {
    return try {
        // Supabase sends ISO-8601 with timezone offset (e.g. 2026-01-15T14:30:00+00:00).
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
        // If the timestamp is ever malformed, fail gracefully to the date portion
        // rather than crashing the feed.
        raw.substringBefore("T").ifBlank { raw }
    }
}