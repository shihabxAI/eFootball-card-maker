// ফাইল: CardMakerApplication.kt
package com.example.efootballcardmaker3

import android.app.Application

class CardMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // অ্যাপ চালু হওয়ার সময় সেভ করা থিমটি লোড করা হচ্ছে
        val savedTheme = ThemeManager.getThemeMode(this)
        ThemeManager.applyTheme(savedTheme)
    }
}