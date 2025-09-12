package com.example.efootballcardmaker3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.efootballcardmaker3.databinding.ItemWhatsNewFeatureBinding
import com.example.efootballcardmaker3.databinding.ItemWhatsNewMainHeaderBinding
import com.example.efootballcardmaker3.databinding.ItemWhatsNewSectionHeaderBinding

class WhatsNewAdapter(private val items: List<WhatsNewItem>) : RecyclerView.Adapter<WhatsNewAdapter.BaseViewHolder>() {

    companion object {
        private const val TYPE_MAIN_HEADER = 0
        private const val TYPE_SECTION_HEADER = 1
        private const val TYPE_FEATURE = 2
    }

    abstract class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: WhatsNewItem)
    }

    inner class MainHeaderViewHolder(private val binding: ItemWhatsNewMainHeaderBinding) : BaseViewHolder(binding) {
        override fun bind(item: WhatsNewItem) {
            val headerItem = item as WhatsNewItem.MainHeader
            binding.mainHeaderTitle.text = headerItem.title
            binding.mainHeaderSubtitle.text = headerItem.subtitle
        }
    }

    inner class SectionHeaderViewHolder(private val binding: ItemWhatsNewSectionHeaderBinding) : BaseViewHolder(binding) {
        override fun bind(item: WhatsNewItem) {
            val headerItem = item as WhatsNewItem.SectionHeader
            binding.sectionTitle.text = headerItem.title
            binding.sectionIcon.setImageResource(headerItem.iconResId)
        }
    }

    inner class FeatureViewHolder(private val binding: ItemWhatsNewFeatureBinding) : BaseViewHolder(binding) {
        override fun bind(item: WhatsNewItem) {
            val featureItem = item as WhatsNewItem.Feature
            binding.featureDescription.text = featureItem.description
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is WhatsNewItem.MainHeader -> TYPE_MAIN_HEADER
            is WhatsNewItem.SectionHeader -> TYPE_SECTION_HEADER
            is WhatsNewItem.Feature -> TYPE_FEATURE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_MAIN_HEADER -> MainHeaderViewHolder(ItemWhatsNewMainHeaderBinding.inflate(inflater, parent, false))
            TYPE_SECTION_HEADER -> SectionHeaderViewHolder(ItemWhatsNewSectionHeaderBinding.inflate(inflater, parent, false))
            TYPE_FEATURE -> FeatureViewHolder(ItemWhatsNewFeatureBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}