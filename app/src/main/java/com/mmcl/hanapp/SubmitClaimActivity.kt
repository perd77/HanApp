package com.mmcl.hanapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mmcl.hanapp.data.remote.dto.CreateClaimRequest
import com.mmcl.hanapp.data.repository.ItemRepository
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivitySubmitClaimBinding
import com.mmcl.hanapp.util.InputValidator
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch

// Form for submitting a claim on a FOUND item, or reporting "I have this
// item" for a LOST post. Same three fields either way; only the copy differs.
class SubmitClaimActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmitClaimBinding
    private lateinit var session: SessionManager
    private val repository = ItemRepository()

    companion object {
        private const val EXTRA_ITEM_ID = "extra_item_id"
        private const val EXTRA_POST_TYPE = "extra_post_type"

        fun newIntent(context: Context, itemId: String, postType: String): Intent {
            return Intent(context, SubmitClaimActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_POST_TYPE, postType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitClaimBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setupCopyForPostType()
        binding.buttonClaimClose.setOnClickListener { finish() }
        binding.buttonSubmitClaim.setOnClickListener { attemptSubmit() }
    }

    // Swaps the title/subtitle wording depending on whether this is a claim
    // on a found item, or an "I have this" report on a lost post.
    private fun setupCopyForPostType() {
        val postType = intent.getStringExtra(EXTRA_POST_TYPE).orEmpty()
        if (postType == "LOST") {
            binding.textClaimTitle.text = getString(R.string.claim_title_lost)
            binding.textClaimSubtitle.text = getString(R.string.claim_subtitle_lost)
        } else {
            binding.textClaimTitle.text = getString(R.string.claim_title_found)
            binding.textClaimSubtitle.text = getString(R.string.claim_subtitle_found)
        }
    }

    private fun attemptSubmit() {
        val contact = binding.editTextContact.text?.toString().orEmpty().trim()
        val proof = binding.editTextProof.text?.toString().orEmpty().trim()

        val contactResult = InputValidator.validateContactNumber(contact)
        if (contactResult is InputValidator.Result.Invalid) {
            binding.inputLayoutContact.error = contactResult.reason
            return
        }
        binding.inputLayoutContact.error = null

        if (proof.isEmpty()) {
            binding.inputLayoutProof.error = "Please describe your proof"
            return
        }
        binding.inputLayoutProof.error = null

        submitClaim(contact, proof)
    }

    private fun submitClaim(contact: String, proof: String) {
        val userId = session.getUserId()
        val username = session.getUsername()
        val itemIdString = intent.getStringExtra(EXTRA_ITEM_ID)
        val itemId = itemIdString?.toLongOrNull()

        if (userId == null || username == null || itemId == null) {
            binding.inputLayoutContact.error = "Session error. Please try again."
            return
        }

        setLoading(true)
        val request = CreateClaimRequest(
            itemId = itemId,
            claimedBy = username,
            contactNumber = contact,
            proofDescription = proof,
            userId = userId
        )

        lifecycleScope.launch {
            when (val result = repository.createClaim(request)) {
                is NetworkResult.Success -> {
                    setResult(RESULT_OK)
                    finish()
                }
                is NetworkResult.Error -> {
                    setLoading(false)
                    binding.inputLayoutContact.error = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressClaimSubmit.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonSubmitClaim.isEnabled = !loading
    }
}