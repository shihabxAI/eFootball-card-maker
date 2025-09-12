package com.example.efootballcardmaker3

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import java.io.File
import io.github.rupinderjeet.kprogresshud.KProgressHUD;
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.efootballcardmaker3.databinding.ActivityPotwBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PotwActivity : BaseActivity(), CountrySearchDialogFragment.OnCountrySelectedListener,
    EraseableImageView.OnDrawHistoryChangedListener {

    private lateinit var binding: ActivityPotwBinding
    private lateinit var hud: KProgressHUD
    private val countryList by lazy { createCountryList() }
    private var isEraserMode = false

    private val flagPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadFlagIntoImageViews(it) }
    }

    private val playerImagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.previewPlayerImage.loadImage(it) {
                Toast.makeText(this, "Image loaded. Tap 'Eraser' to edit.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Note: playerImagePickerLauncher2 এবং btnAddPlayerImage2 এর ব্যবহার আপনার কোডে ছিল কিন্তু player1 নামে কোনো ভিউ নেই।
    // আমি অপ্রয়োজনীয় অংশটুকু আপাতত কমেন্ট আউট করে রাখছি। প্রয়োজন হলে ঠিক করে নিতে পারবেন।
    
    private val playerImagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .into(binding.player1) // 'player1' নামে কোনো ভিউ নেই
        }
    }
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPotwBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
          // 👇 এই অংশটুকু যোগ করুন
    hud = KProgressHUD.create(this)
        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
        .setLabel("Card Creating...")
        .setDetailsLabel("Please wait a moment")
        .setCancellable(false)
       // .setAnimation(KProgressHUD.Animation.FADE)
        .setDimAmount(0.5f)
        
        setupToolbar(
            toolbarId = R.id.potw_toolbar,
            title = "POTW card editor",
            showBackButton = true
        )
        
        binding.previewPlayerImage.historyChangedListener = this
        setupInteractiveUI()
        resetToDefaults()
    }
    
    override fun onHistoryChanged(canUndo: Boolean, canRedo: Boolean) {
        binding.controls.btnUndo.isEnabled = canUndo
        binding.controls.btnRedo.isEnabled = canRedo
    }

    private fun setupInteractiveUI() {
        binding.controls.inputPlayerName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.previewPlayerName.text =
                    if (s.isNullOrEmpty()) getString(R.string.preview_default_player_name) else s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.controls.inputRating.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.previewRatingVertical.text =
                    if (s.isNullOrEmpty()) getString(R.string.previewRating) else s
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.controls.positionSpinner.setOnClickListener { showPositionSelectionDialog() }
        binding.controls.countryClubSpinner.setOnClickListener { showCountrySearchDialog() }
        binding.controls.btnUploadLogo.setOnClickListener { flagPickerLauncher.launch("image/*") }
        binding.controls.btnAddPlayerImage1.setOnClickListener { playerImagePickerLauncher.launch("image/*") }
        binding.controls.btnAddPlayerImage2.setOnClickListener { playerImagePickerLauncher2.launch("image/*") }

        binding.controls.btnToggleEraser.setOnClickListener { toggleEraserMode() }
        binding.controls.btnUndo.setOnClickListener { binding.previewPlayerImage.undo() }
        binding.controls.btnRedo.setOnClickListener { binding.previewPlayerImage.redo() }

        binding.controls.eraserSizeSlider.addOnChangeListener { _, value, _ ->
            binding.previewPlayerImage.eraserSize = value
        }

        binding.controls.eraserHardnessSlider.addOnChangeListener { _, value, _ ->
            binding.previewPlayerImage.eraserHardness = value / 100f
        }

        binding.controls.btnResetCard.setOnClickListener { resetToDefaults() }
        
        // 💡 shareCard এর listener মুছে ফেলা হয়েছে
        // binding.btnShareCard.setOnClickListener { shareCard() } 
        
        binding.controls.btnDownloadCard.setOnClickListener { downloadCard() }
    }

    private fun toggleEraserMode() {
        isEraserMode = !isEraserMode
        binding.previewPlayerImage.isEraserEnabled = isEraserMode

        if (isEraserMode) {
            binding.previewPlayerImage.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            binding.controls.eraserSizeLbl.visibility = View.VISIBLE
            binding.controls.eraserSizeSlider.visibility = View.VISIBLE
            binding.controls.eraserHardnessLbl.visibility = View.VISIBLE
            binding.controls.eraserHardnessSlider.visibility = View.VISIBLE
            Toast.makeText(this, "Eraser Mode ON", Toast.LENGTH_SHORT).show()
        } else {
            binding.previewPlayerImage.setLayerType(View.LAYER_TYPE_NONE, null)
            binding.controls.eraserSizeLbl.visibility = View.GONE
            binding.controls.eraserSizeSlider.visibility = View.GONE
            binding.controls.eraserHardnessLbl.visibility = View.GONE
            binding.controls.eraserHardnessSlider.visibility = View.GONE
            Toast.makeText(this, "Eraser Mode OFF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetToDefaults() {
        binding.previewPlayerImage.setImageDrawable(null)
        binding.previewPlayerImage.clearHistory()
        isEraserMode = false
        binding.previewPlayerImage.isEraserEnabled = false
        binding.controls.inputPlayerName.setText("")
        binding.controls.inputRating.setText("")
        binding.previewPlayerName.text = getString(R.string.preview_default_player_name)
        binding.previewRatingVertical.text = getString(R.string.previewRating)
        binding.controls.positionSpinner.text = getString(R.string.hint_select_position)
        binding.controls.positionSpinner.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        binding.previewPositionVertical.text = "AMF"
        binding.controls.countryClubSpinner.text = getString(R.string.hint_select_country)
        binding.controls.countryClubSpinner.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        binding.imgFlag.setImageResource(R.drawable.bd_flag)
        binding.imgFlagStroke.setImageResource(R.drawable.bd_flag)
        binding.controls.eraserSizeLbl.visibility = View.GONE
        binding.controls.eraserSizeSlider.visibility = View.GONE
        binding.controls.eraserHardnessLbl.visibility = View.GONE
        binding.controls.eraserHardnessSlider.visibility = View.GONE
        binding.controls.eraserSizeSlider.value = 20f
        binding.previewPlayerImage.eraserSize = 20f
        binding.controls.eraserHardnessSlider.value = 100f
        binding.previewPlayerImage.eraserHardness = 1.0f
    }
    
    // 💡 shareCard() ফাংশনটি মুছে ফেলা হয়েছে
    
    // 💡 downloadCard() ফাংশনটি আপডেট করা হয়েছে
    private fun downloadCard() {
    // ProgressBar দেখানো শুরু করুন এবং বাটনটি নিষ্ক্রিয় করুন
    hud.show()
    binding.controls.btnDownloadCard.isEnabled = false

    lifecycleScope.launch {
        val bitmap = withContext(Dispatchers.Default) {
            captureCardAsBitmap() // Bitmap তৈরির কাজটি ব্যাকগ্রাউন্ডে হবে
        }
        
        // ফাইল সেভ করার কাজটিও ব্যাকগ্রাউন্ডে হবে
        val imageUri = withContext(Dispatchers.IO) {
            saveBitmapToDownloads(bitmap)
        }
        
        releaseBitmap(bitmap)

        // UI সম্পর্কিত কাজ (শেয়ার ডায়ালগ এবং ProgressBar লুকানো) Main Thread-এ হবে
        imageUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Card Via"))
        }

        // ProgressBar লুকান এবং বাটনটি আবার সক্রিয় করুন
        hud.dismiss()
        binding.controls.btnDownloadCard.isEnabled = true
    }
}


    private fun captureCardAsBitmap(): Bitmap {
        val view = binding.cardPreviewContainer
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun releaseBitmap(bitmap: Bitmap?) {
        bitmap?.recycle()
    }
    
    // 💡 saveBitmapToCache() ফাংশনটি মুছে ফেলা হয়েছে কারণ এটি আর দরকার নেই

    // 💡 saveBitmapToDownloads() ফাংশনটি আপডেট করা হয়েছে, এটি এখন Uri রিটার্ন করে
    private suspend fun saveBitmapToDownloads(bitmap: Bitmap): Uri? {
        val filename = "EFCARD_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + "EFCARDIFY")
            }
        }
        return withContext(Dispatchers.Main) {
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            try {
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
                Toast.makeText(this@PotwActivity, getString(R.string.card_saved_success), Toast.LENGTH_SHORT).show()
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PotwActivity, getString(R.string.card_saved_failed), Toast.LENGTH_SHORT).show()
                null
            }
        }
    }

    private fun showPositionSelectionDialog() {
        val positions = resources.getStringArray(R.array.player_positions)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_position))
            .setItems(positions) { dialog, which ->
                val selectedPosition = positions[which]
                binding.controls.positionSpinner.text = selectedPosition
                binding.previewPositionVertical.text = selectedPosition
                binding.controls.positionSpinner.setTextColor(ContextCompat.getColor(this, R.color.black))
                dialog.dismiss()
            }
            .show()
    }

    override fun onCountrySelected(country: Country) {
        binding.controls.countryClubSpinner.text = country.name
        binding.controls.countryClubSpinner.setTextColor(ContextCompat.getColor(this, R.color.black))
        loadFlagIntoImageViews(country.flagUrl)
    }

    private fun <T> loadFlagIntoImageViews(model: T) {
        Glide.with(this).load(model).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgFlag)
        Glide.with(this).load(model).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgFlagStroke)
    }

    private fun showCountrySearchDialog() {
        val dialog = CountrySearchDialogFragment(countryList)
        dialog.listener = this
        dialog.show(supportFragmentManager, "CountrySearchDialog")
    }

    private fun createCountryList(): List<Country> = listOf(
        Country("Argentina", "ar"), Country("Bangladesh", "bd"), Country("Brazil", "br"),
        Country("Belgium", "be"), Country("Canada", "ca"), Country("Denmark", "dk"),
        Country("Egypt", "eg"), Country("France", "fr"), Country("Germany", "de"),
        Country("India", "in"), Country("Italy", "it"), Country("Japan", "jp"),
        Country("Netherlands", "nl"), Country("Portugal", "pt"), Country("Spain", "es"),
        Country("Saudi Arabia", "sa"), Country("Turkey", "tr"), Country("United States", "us"),
        Country("United Kingdom", "gb")
    )
}
