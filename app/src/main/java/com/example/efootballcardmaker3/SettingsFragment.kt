package com.example.efootballcardmaker3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.efootballcardmaker3.databinding.FragmentSettingBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettingsList()
    }
    
   // ফেসবুক প্রোফাইল খোলার জন্য ফাংশন
private fun openFacebookProfile() {

    // ⚠️ নিচের দুটি লাইন আপনার নিজের তথ্য দিয়ে পূরণ করুন

    val facebookUsername = "muhammadshihab55" // আপনার ফেসবুক ইউজারনেম

    val facebookUserId = "100047886198316"   // ধাপ ১ থেকে পাওয়া আপনার নিউমেরিক আইডি



    try {

        // প্রথমে ফেসবুক অ্যাপে খোলার চেষ্টা করা হবে (সবচেয়ে নির্ভরযোগ্য পদ্ধতি)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/$facebookUserId"))

        startActivity(intent)

    } catch (e: Exception) {

        // যদি ফেসবুক অ্যাপ না থাকে বা উপরের লিঙ্কে কাজ না করে, তাহলে ব্রাউজারে খোলা হবে

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/$facebookUsername"))

        startActivity(intent)

    }

}



    // --- নতুন কোড ---
    // এই Fragment টি যখনই স্ক্রিনে আসবে, এই ফাংশনটি কল হবে
    override fun onResume() {
        super.onResume()
        // MainActivity-কে খুঁজে তার টাইটেল "Setting" সেট করতে বলা হচ্ছে
        (activity as? MainActivity)?.setToolbarTitle(getString(R.string.settings_title))
    }

    private fun setupSettingsList() {
        val settingsItems = listOf(
            SettingItem("dark_mode", R.drawable.ic_home, getString(R.string.dark_mode), getString(R.string.dark_mode_summary), hasSwitch = true),
            SettingItem("notification", R.drawable.ic_home, getString(R.string.notification), getString(R.string.notification_summary), hasSwitch = true),
            SettingItem("contact", R.drawable.ic_home, getString(R.string.contact_with_me), getString(R.string.contact_summary)),
            SettingItem("whats_new", R.drawable.ic_home, getString(R.string.whats_new), getString(R.string.whats_new_summary)),
            SettingItem("version", R.drawable.ic_home, getString(R.string.version), getString(R.string.version_number), isVersion = true)
        )

        val settingsAdapter = SettingsAdapter(
            items = settingsItems,
            onItemClick = { item ->
                when (item.id) {
                    "contact" -> openFacebookProfile()
                   "whats_new" -> {
            // নতুন বটম শীটটি দেখানো হচ্ছে
            val whatsNewSheet = WhatsNewBottomSheet()
            whatsNewSheet.show(parentFragmentManager, WhatsNewBottomSheet.TAG)
        }
                    "version" -> Toast.makeText(context, "Version clicked!", Toast.LENGTH_SHORT).show()
                }
            },
            onSwitchChange = { item, isChecked ->
                when (item.id) {
                    "dark_mode" -> {
                        val message = if (isChecked) "Dark Mode On" else "Dark Mode Off"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                    "notification" -> {
                        val message = if (isChecked) "Notification Enabled" else "Notification Disabled"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.settingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), 56))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}