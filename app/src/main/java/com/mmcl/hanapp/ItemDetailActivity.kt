package com.mmcl.hanapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivityItemDetailBinding
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.ItemStatus

// Shows full details for one item, with a Claim / "I Have This Item" button
// shown only when the viewer is NOT the original poster and the item is
// still unclaimed. A plain-English banner above the button clarifies exactly
// what the action means, so it's unambiguous regardless of which tab the
// viewer arrived from.
class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var session: SessionManager

    companion object {
        private const val EXTRA_ITEM_ID = "extra_item_id"
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_DESCRIPTION = "extra_description"
        private const val EXTRA_CATEGORY = "extra_category"
        private const val EXTRA_LOCATION = "extra_location"
        private const val EXTRA_STATUS = "extra_status"
        private const val EXTRA_POSTED_BY = "extra_posted_by"
        private const val EXTRA_TIMESTAMP = "extra_timestamp"
        private const val EXTRA_PHOTO_URL = "extra_photo_url"
        private const val EXTRA_OWNER_USER_ID = "extra_owner_user_id"
        private const val EXTRA_POST_TYPE = "extra_post_type"

        fun newIntent(context: Context, item: Item, ownerUserId: String?): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, item.id)
                putExtra(EXTRA_NAME, item.name)
                putExtra(EXTRA_DESCRIPTION, item.description)
                putExtra(EXTRA_CATEGORY, item.category.displayName)
                putExtra(EXTRA_LOCATION, item.locationTag)
                putExtra(EXTRA_STATUS, item.status.name)
                putExtra(EXTRA_POSTED_BY, item.postedBy)
                putExtra(EXTRA_TIMESTAMP, item.timestampLabel)
                putExtra(EXTRA_PHOTO_URL, item.photoUrl)
                putExtra(EXTRA_OWNER_USER_ID, ownerUserId)
                putExtra(EXTRA_POST_TYPE, item.postType.name)
            }
        }
    }

    private val submitClaimLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.claim_success_toast), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        bindItemData()
        binding.buttonDetailClose.setOnClickListener { finish() }
    }

    private fun bindItemData() {
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()
        val category = intent.getStringExtra(EXTRA_CATEGORY).orEmpty()
        val location = intent.getStringExtra(EXTRA_LOCATION).orEmpty()
        val status = intent.getStringExtra(EXTRA_STATUS).orEmpty()
        val postedBy = intent.getStringExtra(EXTRA_POSTED_BY).orEmpty()
        val timestamp = intent.getStringExtra(EXTRA_TIMESTAMP).orEmpty()
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        val ownerUserId = intent.getStringExtra(EXTRA_OWNER_USER_ID)
        val postType = intent.getStringExtra(EXTRA_POST_TYPE).orEmpty()
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID).orEmpty()

        binding.textDetailName.text = name
        binding.textDetailDescription.text = description
        binding.tagDetailCategory.text = category
        binding.tagDetailLocation.text = location
        binding.textDetailMeta.text = getString(R.string.detail_posted_by_format, postedBy, timestamp)

        if (!photoUrl.isNullOrBlank()) {
            binding.imageDetailPhoto.load(photoUrl)
        }

        if (postType == "FOUND") {
            binding.badgeDetailStatus.visibility = View.VISIBLE
            if (status == ItemStatus.CLAIMED.name) {
                binding.badgeDetailStatus.text = getString(R.string.status_claimed)
                binding.badgeDetailStatus.setBackgroundResource(R.drawable.bg_badge_claimed)
                binding.badgeDetailStatus.setTextColor(getColor(R.color.status_claimed))
            } else {
                binding.badgeDetailStatus.text = getString(R.string.status_unclaimed)
                binding.badgeDetailStatus.setBackgroundResource(R.drawable.bg_badge_unclaimed)
                binding.badgeDetailStatus.setTextColor(getColor(R.color.status_unclaimed))
            }
        } else {
            binding.badgeDetailStatus.visibility = View.GONE
        }

        // Button text AND the plain-English banner above it both switch
        // based on post type, so the intended action is stated twice,
        // in two different ways, removing any ambiguity.
        if (postType == "LOST") {
            binding.buttonClaim.text = getString(R.string.detail_have_item_button)
            binding.textContextBanner.text = getString(R.string.detail_context_lost)
        } else {
            binding.buttonClaim.text = getString(R.string.detail_claim_button)
            binding.textContextBanner.text = getString(R.string.detail_context_found)
        }

        val currentUserId = session.getUserId()
        val isOwner = currentUserId != null && currentUserId == ownerUserId
        val isClaimable = status == ItemStatus.UNCLAIMED.name

        when {
            isOwner -> {
                binding.buttonClaim.visibility = View.GONE
                binding.layoutContextBanner.visibility = View.GONE
                binding.textOwnPostNotice.visibility = View.VISIBLE
            }
            isClaimable -> {
                binding.buttonClaim.visibility = View.VISIBLE
                binding.layoutContextBanner.visibility = View.VISIBLE
                binding.textOwnPostNotice.visibility = View.GONE
                binding.buttonClaim.setOnClickListener {
                    submitClaimLauncher.launch(
                        SubmitClaimActivity.newIntent(this, itemId, postType)
                    )
                }
            }
            else -> {
                binding.buttonClaim.visibility = View.GONE
                binding.layoutContextBanner.visibility = View.GONE
                binding.textOwnPostNotice.visibility = View.GONE
            }
        }
    }
}