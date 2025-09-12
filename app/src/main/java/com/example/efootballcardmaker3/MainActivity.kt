package com.example.efootballcardmaker3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.efootballcardmaker3.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar সেটআপ করা হলো
        setupToolbar(
            toolbarId = R.id.home_toolbar,
            title = "", 
            showBackButton = false
        )
        
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment()) 
                    true
                }
                
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // --- এই ফাংশনটি সম্পূর্ণ নতুন করে লেখা হয়েছে ---
    fun setToolbarTitle(title: String) {
        // View Binding এর মাধ্যমে সরাসরি TextView কে অ্যাক্সেস করা হচ্ছে
        // এখানে কোনো findViewById নেই
        binding.homeToolbar.toolbarTitle.text = title
    }
}