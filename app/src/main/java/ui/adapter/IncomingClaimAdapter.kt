package com.mmcl.hanapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmcl.hanapp.R
import com.mmcl.hanapp.databinding.ItemNotificationIncomingBinding
import com.mmcl.hanapp.ui.model.ClaimApprovalStatus
import com.mmcl.hanapp.ui.model.NotificationClaim
import com.mmcl.hanapp.ui.model.PostType

// Shows claims submitted on the current user's own posts, with actions while
// pending, or a resolved status label once acted on. Button wording adapts
// to the post type: Approve/Reject for a claim on a FOUND item (verifying
// ownership), vs This Is Mine/Not Mine for a LOST-post response (someone
// offering to return an item, not asking for permission).
class IncomingClaimAdapter(
    private val onApprove: (NotificationClaim) -> Unit,
    private val onReject: (NotificationClaim) -> Unit
) : ListAdapter<NotificationClaim, IncomingClaimAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationIncomingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onApprove, onReject)
    }

    class ViewHolder(
        private val binding: ItemNotificationIncomingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            claim: NotificationClaim,
            onApprove: (NotificationClaim) -> Unit,
            onReject: (NotificationClaim) -> Unit
        ) {
            val context = binding.root.context

            binding.textClaimItemName.text = claim.itemName
            binding.textClaimantInfo.text = context.getString(
                R.string.notification_claimant_format, claim.claimantName, claim.contactNumber
            )
            binding.textClaimProof.text = "\"${claim.proofDescription}\""
            binding.textClaimTimestamp.text = claim.createdAtLabel

            if (claim.status == ClaimApprovalStatus.PENDING) {
                binding.layoutClaimActions.visibility = View.VISIBLE
                binding.textClaimResolvedStatus.visibility = View.GONE

                // Wording depends on what kind of post this claim is on.
                if (claim.itemPostType == PostType.LOST) {
                    binding.buttonApprove.text = context.getString(R.string.notification_confirm_mine)
                    binding.buttonReject.text = context.getString(R.string.notification_not_mine)
                } else {
                    binding.buttonApprove.text = context.getString(R.string.notification_approve)
                    binding.buttonReject.text = context.getString(R.string.notification_reject)
                }

                binding.buttonApprove.setOnClickListener { onApprove(claim) }
                binding.buttonReject.setOnClickListener { onReject(claim) }
            } else {
                binding.layoutClaimActions.visibility = View.GONE
                binding.textClaimResolvedStatus.visibility = View.VISIBLE
                val isApproved = claim.status == ClaimApprovalStatus.APPROVED
                binding.textClaimResolvedStatus.text = context.getString(
                    if (isApproved) R.string.notification_approved else R.string.notification_rejected
                )
                binding.textClaimResolvedStatus.setBackgroundResource(
                    if (isApproved) R.drawable.bg_badge_unclaimed else R.drawable.bg_badge_claimed
                )
                binding.textClaimResolvedStatus.setTextColor(
                    context.getColor(if (isApproved) R.color.status_unclaimed else R.color.status_claimed)
                )
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NotificationClaim>() {
        override fun areItemsTheSame(oldItem: NotificationClaim, newItem: NotificationClaim) =
            oldItem.claimId == newItem.claimId
        override fun areContentsTheSame(oldItem: NotificationClaim, newItem: NotificationClaim) =
            oldItem == newItem
    }
}