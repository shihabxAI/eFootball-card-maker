// ফাইল: HomeFragment.kt
package com.example.efootballcardmaker3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.efootballcardmaker3.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var resourcesAdapter: ResourcesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupNavigationClickListeners()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupNavigationClickListeners() {
        binding.cardEpic.setOnClickListener { navigateToActivity(EpicActivity::class.java) }
        binding.cardPotw.setOnClickListener { navigateToActivity(PotwActivity::class.java) }
        binding.cardNewEpic.setOnClickListener { navigateToActivity(NewEpicActivity::class.java) }
    }

    private fun setupRecyclerView() {
        resourcesAdapter = ResourcesAdapter { resourceItem ->
            openUrl(resourceItem.url)
        }
        
        binding.resourcesRecyclerView.apply {
            adapter = resourcesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), 0))
        }
    }

    private fun observeViewModel() {
        viewModel.resources.observe(viewLifecycleOwner) { resourceList ->
            resourcesAdapter.submitList(resourceList)
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(requireActivity(), activityClass)
        startActivity(intent)
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not open website.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}