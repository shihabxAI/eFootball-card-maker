// ফাইল: BaseEditorViewModel.kt (সম্পূর্ণ এবং চূড়ান্ত কোড)
package com.example.efootballcardmaker3

import android.content.ContentResolver
import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// 👇 ডেটা ক্লাসটি এখানে সঠিকভাবে ডিফাইন করা হয়েছে
data class EditorUiState(
    val playerName: String = "Player Name",
    val rating: String = "101",
    val position: String = "AMF",
    val countryName: String = "Select Country",
    val flagUrl: Any? = R.drawable.bd_flag,
    val positionTextColorRes: Int = android.R.color.darker_gray,
    val countryTextColorRes: Int = android.R.color.darker_gray
)

abstract class BaseEditorViewModel : ViewModel() {

    protected val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableStateFlow<Event?>(null)
    val events = _events.asStateFlow()

    private val _coinBalance = MutableStateFlow(0)
    val coinBalance = _coinBalance.asStateFlow()
    private val COIN_COST_PER_GENERATION = 2

    fun loadInitialCoins(context: Context, cardType: CardType) {
        val currentCoins = CoinRepository.getCoinBalance(context, cardType)
        _coinBalance.value = currentCoins
    }

    fun onPlayerNameChanged(name: CharSequence?) {
        _uiState.update { it.copy(playerName = if (name.isNullOrEmpty()) "Player Name" else name.toString()) }
    }

    fun onRatingChanged(rating: CharSequence?) {
        _uiState.update { it.copy(rating = if (rating.isNullOrEmpty()) "101" else rating.toString()) }
    }

    fun onPositionSelected(position: String) {
        _uiState.update { it.copy(
            position = position,
            positionTextColorRes = R.color.black
        )}
    }

    fun onCountrySelected(country: Country) {
        _uiState.update { it.copy(
            countryName = country.name,
            flagUrl = country.flagUrl,
            countryTextColorRes = R.color.black
        )}
    }

    fun onFlagSelected(uri: Uri) {
         _uiState.update { it.copy(flagUrl = uri) }
    }

    open fun onReset() {
        _uiState.value = EditorUiState()
    }

    fun onDownloadCard(context: Context, cardBitmap: Bitmap, cardType: CardType) {
        viewModelScope.launch {
            val success = CoinRepository.deductCoins(context, cardType, COIN_COST_PER_GENERATION)

            if (success) {
                _events.value = Event.ShowLoading("Creating Card...")
                val uri = saveBitmapToDownloads(context.contentResolver, cardBitmap, cardType.name)
                cardBitmap.recycle()
                _events.value = Event.HideLoading

                if (uri != null) {
                    _events.value = Event.ShareCard(uri)
                } else {
                    _events.value = Event.ShowToast("Failed to save card.")
                }
                
                loadInitialCoins(context, cardType)

            } else {
                _events.value = Event.ShowToast("Not enough coins to generate card!")
            }
        }
    }

    fun consumeEvent() {
        _events.value = null
    }

    private suspend fun saveBitmapToDownloads(resolver: ContentResolver, bitmap: Bitmap, cardType: String): Uri? = withContext(Dispatchers.IO) {
        val filename = "EFCARD_${cardType.uppercase()}_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + "EFCARDIFY")
            }
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        try {
            uri?.let { resolver.openOutputStream(it)?.use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) } }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    sealed class Event {
        data class ShowToast(val message: String) : Event()
        data class ShareCard(val uri: Uri) : Event()
        data class ShowLoading(val message: String) : Event()
        object HideLoading : Event()
    }
}