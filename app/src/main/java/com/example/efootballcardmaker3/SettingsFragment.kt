package com.example.efootballcardmaker3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.efootballcardmaker3.databinding.FragmentSettingBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel যোগ করা হয়েছে
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        
        // ViewModel-কে ডেটা লোড করার নির্দেশ দেওয়া হচ্ছে
        viewModel.loadSettings(requireContext())
    }

    private fun setupRecyclerView() {
        // নতুন Adapter ব্যবহার করা হচ্ছে
        settingsAdapter = SettingsAdapter(
            onItemClick = { item ->
                handleItemClick(item)
            },
            onSwitchChange = { item, isChecked ->
                if (item.id == "dark_mode") {
                    viewModel.onDarkModeToggled(isChecked, requireContext())
                } else if (item.id == "notification") {
                    val message = if (isChecked) "Notification Enabled" else "Notification Disabled"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.settingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), 56))
        }
    }
    
    // ViewModel থেকে ডেটা পর্যবেক্ষণ করার জন্য নতুন ফাংশন
    private fun observeViewModel() {
        viewModel.settingsItems.observe(viewLifecycleOwner) { items ->
            // ListAdapter-এ ডেটা পাঠানোর জন্য submitList ব্যবহার করা হয়
            settingsAdapter.submitList(items)
        }
    }
    
    private fun handleItemClick(item: SettingItem) {
        when (item.id) {
            "contact" -> openFacebookProfile()
            "whats_new" -> {
                val whatsNewSheet = WhatsNewBottomSheet()
                whatsNewSheet.show(parentFragmentManager, WhatsNewBottomSheet.TAG)
            }
            "version" -> Toast.makeText(context, "Version clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFacebookProfile() {
        val facebookUsername = "muhammadshihab55"
        val facebookUserId = "100047886198316"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/$facebookUserId")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/$facebookUsername")))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}