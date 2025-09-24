// ফাইল: NavigationDestination.kt
package com.example.efootballcardmaker3

import androidx.annotation.IdRes

// Sealed class for type-safe navigation handling
sealed class NavigationDestination(val itemId: Int) {
    object Home : NavigationDestination(R.id.homeFragment)
    object Settings : NavigationDestination(R.id.settingFragment)

    companion object {
        fun fromItemId(@IdRes itemId: Int): NavigationDestination? {
            return when (itemId) {
                R.id.homeFragment -> Home
                R.id.settingFragment -> Settings
                else -> null
            }
        }
    }
}