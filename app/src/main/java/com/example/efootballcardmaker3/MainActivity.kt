// MainActivity.kt
package com.example.efootballcardmaker3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.efootballcardmaker3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar-কে অ্যাপের ActionBar হিসেবে সেট করুন
        setSupportActionBar(binding.homeToolbar.toolbar)
        // ১. ডিফল্ট টাইটেল দেখানো বন্ধ করুন
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // ২. Fragment পরিবর্তনের সাথে সাথে কাস্টম TextView-এর লেখা আপডেট করুন
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.homeToolbar.toolbarTitle.text = destination.label
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}