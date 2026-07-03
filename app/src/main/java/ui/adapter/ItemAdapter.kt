package com.mmcl.hanapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmcl.hanapp.R
import com.mmcl.hanapp.databinding.ItemFoundPlaceholderBinding
import com.mmcl.hanapp.ui.model.Item
import com.mmcl.hanapp.ui.model.ItemStatus

// Feeds a list of Item objects into the RecyclerView and turns each one into a card view.
// Built on ListAdapter (instead of a plain RecyclerView.Adapter) so that when the list
// updates later from a live API call, only the changed rows animate/redraw — not the whole list.
class ItemAdapter(
    private val onItemClick: (Item) -> Unit // called when a card is tapped, e.g. to open item details
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    // Inflates one empty card layout to be reused/recycled as the user scrolls.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemFoundPlaceholderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    // Fills one recycled card with the data for the item at this position.
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    // Holds references to the views inside one card so we don't re-find them on every scroll.
    class ItemViewHolder(
        private val binding: ItemFoundPlaceholderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Pushes one Item's data into the actual on-screen views.
        fun bind(item: Item, onItemClick: (Item) -> Unit) {
            val context = binding.root.context

            binding.textItemName.text = item.name
            binding.textItemDescription.text = item.description
            binding.tagCategory.text = item.category.displayName
            binding.tagLocation.text = item.locationTag // shows where the item was found
            binding.textTimestamp.text = item.timestampLabel

            // Status badge color/text/background switches based on the item's real status —
            // never hardcoded, so a Claimed item can never accidentally show as Unclaimed.
            when (item.status) {
                ItemStatus.UNCLAIMED -> {
                    binding.badgeStatus.text = context.getString(R.string.status_unclaimed)
                    binding.badgeStatus.setBackgroundResource(R.drawable.bg_badge_unclaimed)
                    binding.badgeStatus.setTextColor(
                        context.getColor(R.color.status_unclaimed)
                    )
                }
                ItemStatus.CLAIMED -> {
                    binding.badgeStatus.text = context.getString(R.string.status_claimed)
                    binding.badgeStatus.setBackgroundResource(R.drawable.bg_badge_claimed)
                    binding.badgeStatus.setTextColor(
                        context.getColor(R.color.status_claimed)
                    )
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    // Tells the RecyclerView how to detect what actually changed between list updates,
    // so it can animate only the changed rows instead of redrawing everything.
    private class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        // Are these two entries the same item (by ID), even if some fields changed?
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem.id == newItem.id

        // Given it's the same item, did any of its actual field values change?
        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem == newItem
    }
}