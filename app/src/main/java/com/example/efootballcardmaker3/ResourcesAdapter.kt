// ফাইল: ResourcesAdapter.kt
package com.example.efootballcardmaker3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.efootballcardmaker3.databinding.ListItemResourceBinding

class ResourcesAdapter(
    private val onItemClicked: (ResourceItem) -> Unit
) : ListAdapter<ResourceItem, ResourcesAdapter.ResourceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ListItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResourceViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ResourceViewHolder(
        private val binding: ListItemResourceBinding,
        private val onItemClicked: (ResourceItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: ResourceItem? = null
        init {
            itemView.setOnClickListener {
                currentItem?.let { onItemClicked(it) }
            }
        }

        fun bind(item: ResourceItem) {
            currentItem = item
            val context = binding.root.context
            binding.iconResource.setImageResource(item.iconRes)
            binding.titleResource.text = context.getString(item.titleRes)
            binding.subtitleResource.text = context.getString(item.subtitleRes)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ResourceItem>() {
            override fun areItemsTheSame(oldItem: ResourceItem, newItem: ResourceItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ResourceItem, newItem: ResourceItem) = oldItem == newItem
        }
    }
}