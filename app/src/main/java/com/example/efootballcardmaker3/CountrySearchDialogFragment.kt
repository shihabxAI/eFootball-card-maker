// ফাইল: CountrySearchDialogFragment.kt
package com.example.efootballcardmaker3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.efootballcardmaker3.databinding.DialogCountrySearchBinding

class CountrySearchDialogFragment(private val countries: List<Country>) : DialogFragment() {

    private var _binding: DialogCountrySearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var countryAdapter: CountryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCountrySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        
        binding.searchEditText.addTextChangedListener { text ->
            val query = text.toString().lowercase().trim()
            val filteredList = if (query.isEmpty()) {
                countries
            } else {
                countries.filter { it.name.lowercase().contains(query) }
            }
            // submitList ব্যবহার করে Adapter-কে নতুন লিস্ট দেওয়া হচ্ছে
            countryAdapter.submitList(filteredList)
        }
    }

    private fun setupRecyclerView() {
        countryAdapter = CountryAdapter { selectedCountry ->
            // Fragment Result API ব্যবহার করে ডেটা পাঠানো হচ্ছে
            setFragmentResult(REQUEST_KEY, bundleOf(KEY_COUNTRY to selectedCountry))
            dismiss()
        }

        binding.countryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = countryAdapter
        }
        
        countryAdapter.submitList(countries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val TAG = "CountrySearchDialog"
        const val REQUEST_KEY = "country_selection_request"
        const val KEY_COUNTRY = "selected_country"
    }
}