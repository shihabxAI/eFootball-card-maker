package com.example.efootballcardmaker3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.efootballcardmaker3.databinding.BottomSheetWhatsNewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WhatsNewBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWhatsNewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWhatsNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val featureList = getFeaturesList()
        val adapter = WhatsNewAdapter(featureList)
        binding.featuresRecyclerView.adapter = adapter
    }

    private fun getFeaturesList(): List<WhatsNewItem> {
        return listOf(
            WhatsNewItem.MainHeader(
                "🌟 আমাদের অ্যাপের আকর্ষণীয় ফিচারসমূহ 🌟",
                "আপনার স্বপ্নের প্লেয়ার কার্ড তৈরি করুন আগের চেয়েও সহজে! আমাদের অ্যাপে রয়েছে দারুণ সব টুলস এবং কাস্টমাইজেশন অপশন।"
            ),
            WhatsNewItem.SectionHeader("কার্ড তৈরি ও ডিজাইন", R.drawable.ic_home),
            WhatsNewItem.Feature("বিভিন্ন ধরণের কার্ড: New Epic, Epic, POTW"),
            WhatsNewItem.Feature("ডুয়াল প্লেয়ার ইমেজ যোগ করার সুযোগ।"),
            WhatsNewItem.Feature("ফোন থেকে সহজেই প্লেয়ারের ও ক্লাবের লোগো আপলোড।"),
            WhatsNewItem.Feature("প্লেয়ারের নাম, রেটিং এবং পজিশন নিজের মতো করে যোগ করুন।"),

            WhatsNewItem.SectionHeader("শক্তিশালী ইমেজ এডিটিং টুলস", R.drawable.ic_eraser),
            WhatsNewItem.Feature("শক্তিশালী ব্যাকগ্রাউন্ড ইরেজার টুল।"),
            WhatsNewItem.Feature("Undo/Redo করার সুবিধা।"),
            WhatsNewItem.Feature("ছবির আকার ছোট বা বড় করার জন্য স্লাইডার।"),

            WhatsNewItem.SectionHeader("ব্যবহারকারী-বান্ধব ইন্টারফেস", R.drawable.ic_home),
            WhatsNewItem.Feature("সার্চ অপশনসহ দেশ ও ক্লাব নির্বাচন।"),
            WhatsNewItem.Feature("এক ক্লিকেই কার্ড রিসেট করার সুবিধা।"),
            WhatsNewItem.Feature("হাই-কোয়ালিটিতে কার্ড ডাউনলোড করার সুবিধা।")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.featuresRecyclerView.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "WhatsNewBottomSheet"
    }
}