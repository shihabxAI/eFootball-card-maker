package com.example.efootballcardmaker3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.efootballcardmaker3.databinding.ListItemSettingBinding

class SettingsAdapter(
    private val onItemClick: (SettingItem) -> Unit,
    private val onSwitchChange: (SettingItem, Boolean) -> Unit
) : ListAdapter<SettingItem, SettingsAdapter.SettingViewHolder>(SettingDiffCallback()) {

    inner class SettingViewHolder(val binding: ListItemSettingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ListItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            summary.text = item.summary

            // বিভিন্ন ভিউয়ের visibility ঠিক করা
            settingSwitch.visibility = if (item.hasSwitch) View.VISIBLE else View.GONE
            versionText.visibility = if (item.isVersion) View.VISIBLE else View.GONE

            if (item.hasSwitch) {
                settingSwitch.setOnCheckedChangeListener(null)
                settingSwitch.isChecked = item.isSwitchChecked
                settingSwitch.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchChange(item, isChecked)
                }
            }
            
            // UX Improvement: পুরো সারি জুড়ে ক্লিক করার সুবিধা
            root.setOnClickListener {
                if (item.hasSwitch) {
                    settingSwitch.isChecked = !settingSwitch.isChecked
                } else {
                    onItemClick(item)
                }
            }
        }
    }

    // DiffUtil.ItemCallback যোগ করা হয়েছে
    class SettingDiffCallback : DiffUtil.ItemCallback<SettingItem>() {
        override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem == newItem
        }
    }
}