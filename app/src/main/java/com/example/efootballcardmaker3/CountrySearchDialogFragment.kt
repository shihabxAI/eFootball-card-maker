package com.example.efootballcardmaker3;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CountrySearchDialogFragment(private val countries: List<Country>) : DialogFragment() {

    interface OnCountrySelectedListener {
        fun onCountrySelected(country: Country)
    }

    var listener: OnCountrySelectedListener? = null

    private lateinit var adapter: CountryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_country_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recyclerView: RecyclerView = view.findViewById(R.id.country_recycler_view)
        val searchEditText: EditText = view.findViewById(R.id.search_edit_text)

        adapter = CountryAdapter(countries) { selectedCountry ->
            listener?.onCountrySelected(selectedCountry)
            dismiss() // দেশ সিলেক্ট হলে ডায়ালগ বন্ধ হয়ে যাবে
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener {
            adapter.filter(it.toString())
        }
    }
}