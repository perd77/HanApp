package com.mmcl.hanapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmcl.hanapp.R
import com.mmcl.hanapp.databinding.ItemNotificationOutgoingBinding
import com.mmcl.hanapp.ui.model.ClaimApprovalStatus
import com.mmcl.hanapp.ui.model.NotificationClaim

// Shows claims the current user submitted on others' items — read-only,
// just reflects whatever the item owner has decided.
class OutgoingClaimAdapter :
    ListAdapter<NotificationClaim, OutgoingClaimAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationOutgoingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemNotificationOutgoingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(claim: NotificationClaim) {
            val context = binding.root.context

            binding.textOutgoingItemName.text = claim.itemName
            binding.textOutgoingTimestamp.text = context.getString(
                R.string.notification_submitted_format, claim.createdAtLabel
            )

            val (labelRes, bgRes, colorRes) = when (claim.status) {
                ClaimApprovalStatus.PENDING ->
                    Triple(R.string.notification_pending, R.drawable.bg_tag_neutral, R.color.sky_blue_dark)
                ClaimApprovalStatus.APPROVED ->
                    Triple(R.string.notification_approved, R.drawable.bg_badge_unclaimed, R.color.status_unclaimed)
                ClaimApprovalStatus.REJECTED ->
                    Triple(R.string.notification_rejected, R.drawable.bg_badge_claimed, R.color.status_claimed)
            }
            binding.badgeOutgoingStatus.text = context.getString(labelRes)
            binding.badgeOutgoingStatus.setBackgroundResource(bgRes)
            binding.badgeOutgoingStatus.setTextColor(context.getColor(colorRes))
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NotificationClaim>() {
        override fun areItemsTheSame(oldItem: NotificationClaim, newItem: NotificationClaim) =
            oldItem.claimId == newItem.claimId
        override fun areContentsTheSame(oldItem: NotificationClaim, newItem: NotificationClaim) =
            oldItem == newItem
    }
}