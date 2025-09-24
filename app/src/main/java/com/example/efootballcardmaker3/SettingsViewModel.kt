package com.example.efootballcardmaker3

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    // ডেটা ধারণ করার জন্য MutableLiveData ব্যবহার করা হচ্ছে
    private val _settingsItems = MutableLiveData<List<SettingItem>>()
    val settingsItems: LiveData<List<SettingItem>> get() = _settingsItems

    // এই ফাংশনটি Fragment থেকে কল করা হবে
    fun loadSettings(context: Context) {
        viewModelScope.launch {
            val currentTheme = ThemeManager.getThemeMode(context)
            val isDarkModeSwitchChecked = currentTheme == ThemeManager.ThemeMode.DARK

            val items = listOf(
                SettingItem(
                    id = "dark_mode",
                    iconRes = R.drawable.ic_dark,
                    title = context.getString(R.string.dark_mode),
                    summary = context.getString(R.string.dark_mode_summary),
                    hasSwitch = true,
                    isSwitchChecked = isDarkModeSwitchChecked
                ),
                SettingItem(
                    id = "notification",
                    iconRes = R.drawable.ic_notification,
                    title = context.getString(R.string.notification),
                    summary = context.getString(R.string.notification_summary),
                    hasSwitch = true
                ),
                SettingItem(
                    id = "contact",
                    iconRes = R.drawable.ic_contact,
                    title = context.getString(R.string.contact_with_me),
                    summary = context.getString(R.string.contact_summary)
                ),
                SettingItem(
                    id = "whats_new",
                    iconRes = R.drawable.ic_whats_new,
                    title = context.getString(R.string.whats_new),
                    summary = context.getString(R.string.whats_new_summary)
                ),
                SettingItem(
                    id = "version",
                    iconRes = R.drawable.ic_version,
                    title = context.getString(R.string.version),
                    summary = context.getString(R.string.version_number),
                    isVersion = true
                )
            )
            // LiveData-তে ডেটা পোস্ট করা হচ্ছে
            _settingsItems.postValue(items)
        }
    }

    fun onDarkModeToggled(isChecked: Boolean, context: Context) {
        val newTheme = if (isChecked) ThemeManager.ThemeMode.DARK else ThemeManager.ThemeMode.LIGHT
        ThemeManager.setThemeMode(context, newTheme)
    }
}