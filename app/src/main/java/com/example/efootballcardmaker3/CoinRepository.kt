// ফাইল: CoinRepository.kt (সম্পূর্ণ নতুন ফাইল)
package com.example.efootballcardmaker3

import android.content.Context
import android.content.SharedPreferences

// "epic", "potw" ইত্যাদি স্ট্রিং সরাসরি ব্যবহার না করে আমরা একটি enum তৈরি করছি,
// যাতে টাইপের ভুল হওয়ার সম্ভাবনা না থাকে।
enum class CardType {
    EPIC,
    NEW_EPIC,
    POTW
}

object CoinRepository {

    private const val PREFS_NAME = "CoinPrefs"
    private const val DEFAULT_COINS = 100

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * একটি নির্দিষ্ট কার্ড টাইপের জন্য কয়েন ব্যালেন্স প্রদান করে।
     * যদি আগে থেকে কোনো ব্যালেন্স না থাকে (প্রথমবার), তবে ডিফল্ট ১০০ কয়েন সেট করে এবং রিটার্ন করে।
     */
    fun getCoinBalance(context: Context, cardType: CardType): Int {
        val prefs = getPreferences(context)
        val key = cardType.name // enum-এর নামটিকে কী (key) হিসেবে ব্যবহার করা হচ্ছে (যেমন: "EPIC")
        
        if (!prefs.contains(key)) {
            // এই কার্ড টাইপের জন্য প্রথমবার, তাই ডিফল্ট কয়েন সেট করা হচ্ছে
            prefs.edit().putInt(key, DEFAULT_COINS).apply()
            return DEFAULT_COINS
        }
        
        return prefs.getInt(key, DEFAULT_COINS)
    }

    /**
     * একটি কার্ড টাইপের জন্য নির্দিষ্ট পরিমাণ কয়েন কেটে নেয়।
     * সফল হলে true এবং অপর্যাপ্ত কয়েন থাকলে false রিটার্ন করে।
     */
    fun deductCoins(context: Context, cardType: CardType, amount: Int): Boolean {
        val prefs = getPreferences(context)
        val key = cardType.name
        
        val currentBalance = getCoinBalance(context, cardType)

        if (currentBalance < amount) {
            return false // অপর্যাপ্ত কয়েন
        }

        val newBalance = currentBalance - amount
        prefs.edit().putInt(key, newBalance).apply()
        return true
    }
}