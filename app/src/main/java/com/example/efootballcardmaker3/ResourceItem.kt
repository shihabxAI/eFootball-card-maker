// ফাইল: ResourceItem.kt
package com.example.efootballcardmaker3

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class ResourceItem(
    val id: String,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val url: String
)