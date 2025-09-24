// ফাইল: HomeViewModel.kt
package com.example.efootballcardmaker3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _resources = MutableLiveData<List<ResourceItem>>()
    val resources: LiveData<List<ResourceItem>> get() = _resources

    init {
        loadResources()
    }

    private fun loadResources() {
        val resourceList = listOf(
            ResourceItem(
                id = "renders",
                iconRes = R.drawable.ic_player_render,
                titleRes = R.string.player_render_title,
                subtitleRes = R.string.player_render_subtitle,
                url = "https://www.footyrenders.com/"
            ),
            ResourceItem(
                id = "remover",
                iconRes = R.drawable.ic_background_remover,
                titleRes = R.string.bg_remover_title,
                subtitleRes = R.string.bg_remover_subtitle,
                url = "https://unlimitedbg.com/"
            )
        )
        _resources.value = resourceList
    }
}