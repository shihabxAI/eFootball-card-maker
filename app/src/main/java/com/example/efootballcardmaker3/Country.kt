package com.example.efootballcardmaker3;

data class Country(
    val name: String,
    val code: String // যেমন: "bd", "us", "ar"
) {
    // CDN থেকে ফ্ল্যাগের URL তৈরি করার জন্য একটি helper property
    val flagUrl: String
        get() = "https://flagcdn.com/w80/${code.lowercase()}.png"
}