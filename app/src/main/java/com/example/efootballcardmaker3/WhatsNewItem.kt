package com.example.efootballcardmaker3

import androidx.annotation.DrawableRes

sealed class WhatsNewItem {
    data class MainHeader(val title: String, val subtitle: String) : WhatsNewItem()
    data class SectionHeader(val title: String, @DrawableRes val iconResId: Int) : WhatsNewItem()
    data class Feature(val description: String) : WhatsNewItem()
}