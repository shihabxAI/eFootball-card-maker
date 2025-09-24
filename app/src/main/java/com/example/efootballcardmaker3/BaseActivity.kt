// ফাইল: BaseActivity.kt (চূড়ান্ত এবং সম্পূর্ণ কোড)
package com.example.efootballcardmaker3

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupToolbar(
        toolbarId: Int,
        title: String,
        showBackButton: Boolean = false,
        @MenuRes menuResId: Int? = null
    ) {
        val toolbar = findViewById<MaterialToolbar>(toolbarId)
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbar_title)
        val backButton = toolbar.findViewById<ImageButton?>(R.id.toolbar_back_button)

        toolbarTitle.text = title

        // 👇 *** মূল পরিবর্তন এখানেই *** 👇
        // এখন কোডটি প্রথমে চেক করে নিচ্ছে যে backButton লেআউটে আছে কিনা
        backButton?.let { btn ->
            if (showBackButton) {
                btn.visibility = View.VISIBLE
                btn.setOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            } else {
                btn.visibility = View.GONE
            }
        }

        if (menuResId != null) {
            toolbar.inflateMenu(menuResId)
            toolbar.setOnMenuItemClickListener { menuItem ->
                onToolbarMenuItemClick(menuItem.itemId)
                true
            }
        }
    }

    open fun onToolbarMenuItemClick(itemId: Int) {
        // Default implementation
    }
}