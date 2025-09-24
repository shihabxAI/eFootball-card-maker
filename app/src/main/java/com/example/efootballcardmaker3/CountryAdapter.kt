// ফাইল: CountryAdapter.kt
package com.example.efootballcardmaker3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.efootballcardmaker3.databinding.ItemCountryBinding

class CountryAdapter(
    private val onCountryClicked: (Country) -> Unit
) : ListAdapter<Country, CountryAdapter.CountryViewHolder>(CountryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding, onCountryClicked)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CountryViewHolder(
        private val binding: ItemCountryBinding,
        private val onCountryClicked: (Country) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {
            binding.countryNameTextView.text = country.name
            Glide.with(itemView.context)
                .load(country.flagUrl)
                .into(binding.flagImageView)
            
            itemView.setOnClickListener {
                onCountryClicked(country)
            }
        }
    }

    // DiffUtil RecyclerView-কে বুঝতে সাহায্য করে কোন আইটেমটি পরিবর্তন হয়েছে
    class CountryDiffCallback : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }
    }
}