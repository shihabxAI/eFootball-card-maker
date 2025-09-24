// ফাইল: data/DataSource.kt
package com.example.efootballcardmaker3.data

import com.example.efootballcardmaker3.Country

object DataSource {
    fun getCountryList(): List<Country> {
        return listOf(
            Country("Argentina", "ar"), Country("Bangladesh", "bd"), Country("Brazil", "br"),
            Country("Belgium", "be"), Country("Canada", "ca"), Country("Denmark", "dk"),
            Country("Egypt", "eg"), Country("France", "fr"), Country("Germany", "de"),
            Country("India", "in"), Country("Italy", "it"), Country("Japan", "jp"),
            Country("Netherlands", "nl"), Country("Portugal", "pt"), Country("Spain", "es"),
            Country("Saudi Arabia", "sa"), Country("Turkey", "tr"), Country("United States", "us"),
            Country("United Kingdom", "gb")
        )
    }
}
