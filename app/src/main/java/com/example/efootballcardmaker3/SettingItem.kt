// ফাইল: SettingItem.kt
package com.example.efootballcardmaker3

// ডেটা ক্লাস আইটেমগুলোকে ধরে রাখার জন্য
data class SettingItem(
    val id: String,
    val iconRes: Int,
    val title: String,
    val summary: String,
    val hasSwitch: Boolean = false,
    val isVersion: Boolean = false,
    val isSwitchChecked: Boolean = false // <-- এই নতুন প্রপার্টিটি যোগ করা হয়েছে
)