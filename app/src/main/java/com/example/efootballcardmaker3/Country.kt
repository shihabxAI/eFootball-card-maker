// ফাইল: Country.kt
package com.example.efootballcardmaker3

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // <-- এই অ্যানোটেশনটি যোগ করুন
data class Country(
    val name: String,
    val code: String // যেমন: "bd", "us", "ar"
) : Parcelable { // <-- : Parcelable যোগ করুন
    val flagUrl: String
        get() = "https://flagcdn.com/w80/${code.lowercase()}.png"
}