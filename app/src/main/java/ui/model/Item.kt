package com.mmcl.hanapp.ui.model

// Represents whether a found item is still available or already returned to its owner.
enum class ItemStatus {
    UNCLAIMED,
    CLAIMED
}

enum class PostType {
    FOUND,
    LOST
}

// Fixed category list matching the dropdown options defined in the system plan.
// displayName is what the user sees; the enum name itself is what the code/API uses internally.
enum class ItemCategory(val displayName: String) {
    ELECTRONICS("Electronics"),
    BAG("Bag"),
    ID_CARDS("ID/Cards"),
    CLOTHING("Clothing"),
    KEYS("Keys"),
    OTHERS("Others")
}

// A single found item as shown in the newsfeed.
// This same model will later map directly to what the REST API returns from GET /items.
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val category: ItemCategory,
    val locationTag: String,
    val status: ItemStatus,
    val postType: PostType, // FOUND (Discovered feed) or LOST (Finding feed)
    val timestampLabel: String, // pre-formatted display string, e.g. "2h ago"
    val posterUserId: String?, // the real account ID of whoever posted this, for ownership checks
    val postedBy: String, // display name of whoever posted this item
    val photoUrl: String? = null // nullable because the photo upload is optional per the system plan

) {
    // Runs automatically every time an Item is created.
    // Blocks bad data (blank name, oversized text) before it ever reaches the UI,
    // instead of letting a broken item silently render on screen.
    init {
        require(name.isNotBlank()) { "Item name cannot be blank" }
        require(name.length <= 60) { "Item name too long" }
        require(description.length <= 300) { "Description exceeds max length" }
    }
}