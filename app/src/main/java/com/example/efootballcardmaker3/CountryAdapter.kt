package com.example.efootballcardmaker3;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CountryAdapter(
    private var countries: List<Country>,
    private val onCountryClicked: (Country) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    private var filteredCountries: MutableList<Country> = countries.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = filteredCountries[position]
        holder.bind(country)
        holder.itemView.setOnClickListener {
            onCountryClicked(country)
        }
    }

    override fun getItemCount(): Int = filteredCountries.size

    fun filter(query: String) {
        filteredCountries.clear()
        if (query.isEmpty()) {
            filteredCountries.addAll(countries)
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            countries.forEach {
                if (it.name.lowercase().contains(lowerCaseQuery)) {
                    filteredCountries.add(it)
                }
            }
        }
        notifyDataSetChanged()
    }

    class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flagImageView: ImageView = itemView.findViewById(R.id.flag_image_view)
        private val nameTextView: TextView = itemView.findViewById(R.id.country_name_text_view)

        fun bind(country: Country) {
            nameTextView.text = country.name
            Glide.with(itemView.context)
                .load(country.flagUrl)
                .into(flagImageView)
        }
    }
}