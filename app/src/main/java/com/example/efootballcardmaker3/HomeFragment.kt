package com.example.efootballcardmaker3

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.efootballcardmaker3.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    // --- নতুন কোড ---
    // এই Fragment টি যখনই স্ক্রিনে আসবে, এই ফাংশনটি কল হবে
    override fun onResume() {
        super.onResume()
        // MainActivity-কে খুঁজে তার টাইটেল পরিবর্তনের ফাংশনটি কল করা হচ্ছে
        (activity as? MainActivity)?.setToolbarTitle(getString(R.string.toolbar_home))
    }

    private fun setupClickListeners() {
        binding.cardEpic.setOnClickListener {
            navigateToActivity(EpicActivity::class.java)
        }
        binding.cardPotw.setOnClickListener {
            navigateToActivity(PotwActivity::class.java)
        }
        binding.cardNewEpic.setOnClickListener {
            navigateToActivity(NewEpicActivity::class.java)
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(requireActivity(), activityClass)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}