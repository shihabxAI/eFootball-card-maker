package com.example.efootballcardmaker3 // আপনার প্যাকেজের নাম দিন

import android.os.Bundle
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * এটি একটি বেস ক্লাস, যা অ্যাপের সকল অ্যাক্টিভিটির জন্য কমন কাজগুলো করবে।
 * যেমন, টুলবার সেটআপ করা।
 * এই ক্লাসটি abstract কারণ এটি সরাসরি ব্যবহার হবে না, অন্য অ্যাক্টিভিটি একে extend করবে।
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * এই ফাংশনটি যেকোনো অ্যাক্টিভিটি থেকে টুলবার সেটআপ করার জন্য ব্যবহার করা হবে।
     * @param toolbarId লেআউটে থাকা <include> ট্যাগের আইডি।
     * @param title টুলবারে যে টেক্সট দেখানো হবে।
     * @param showBackButton টুলবারের বাম পাশে Back বাটন দেখানো হবে কিনা (ডিফল্ট: false)।
     * @param menuResId টুলবারের ডান পাশে আইকন দেখানোর জন্য মেন্যু রিসোর্স ফাইলের আইডি (ঐচ্ছিক)।
     */
    protected fun setupToolbar(
        toolbarId: Int,
        title: String,
        showBackButton: Boolean = false,
        @MenuRes menuResId: Int? = null
    ) {
        // লেআউট থেকে টুলবার এবং টাইটেল TextView খুঁজে বের করা
        val toolbar = findViewById<MaterialToolbar>(toolbarId)
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbar_title)

        // টাইটেল সেট করা
        toolbarTitle.text = title

        // Back বাটন দেখানোর লজিক
        if (showBackButton) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back) // আপনার back আইকনের নাম দিন
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed() // Back বাটনে ক্লিক করলে আগের স্ক্রিনে ফিরে যাবে
            }
        }

        // ঐচ্ছিক আইকন (মেন্যু) দেখানোর লজিক
        if (menuResId != null) {
            toolbar.inflateMenu(menuResId)
            toolbar.setOnMenuItemClickListener { menuItem ->
                // আইটেম ক্লিক হলে onToolbarMenuItemClick ফাংশনকে জানানো হবে
                onToolbarMenuItemClick(menuItem.itemId)
                true
            }
        }
    }

    /**
     * যে অ্যাক্টিভিটিতে টুলবার আইকন থাকবে, সেখানে এই ফাংশনটি override করে
     * ক্লিকের কাজগুলো লিখতে হবে।
     * এটি 'open' হওয়ায় সব অ্যাক্টিভিটির জন্য একে override করা বাধ্যতামূলক নয়।
     * @param itemId যে মেন্যু আইটেমটিতে ক্লিক করা হয়েছে তার আইডি।
     */
    open fun onToolbarMenuItemClick(itemId: Int) {
        // ডিফল্টরূপে এই ফাংশনটি খালি থাকবে।
    }
}

