package com.example.efootballcardmaker3 // তোমার প্যাকেজ নাম

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.efootballcardmaker3.databinding.ListItemSettingBinding

// ডেটা ক্লাস আইটেমগুলোকে ধরে রাখার জন্য
data class SettingItem(
    val id: String,
    val iconRes: Int,
    val title: String,
    val summary: String,
    val hasSwitch: Boolean = false,
    val isVersion: Boolean = false
)

class SettingsAdapter(
    private val items: List<SettingItem>,
    private val onItemClick: (SettingItem) -> Unit,
    private val onSwitchChange: (SettingItem, Boolean) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

    inner class SettingViewHolder(val binding: ListItemSettingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ListItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            summary.text = item.summary

            if (item.hasSwitch) {
                settingSwitch.visibility = View.VISIBLE
                versionText.visibility = View.GONE
                settingSwitch.setOnCheckedChangeListener(null) 
                settingSwitch.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchChange(item, isChecked)
                }
            } 
            else if (item.isVersion) {
                versionText.visibility = View.VISIBLE
                settingSwitch.visibility = View.GONE
            } 
            else {
                settingSwitch.visibility = View.GONE
                versionText.visibility = View.GONE
            }

            if (!item.hasSwitch) {
                 root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun getItemCount() = items.size
}