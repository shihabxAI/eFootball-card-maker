package com.example.efootballcardmaker3

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.efootballcardmaker3.databinding.ActivityNewEpicBinding
import io.github.rupinderjeet.kprogresshud.KProgressHUD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NewEpicActivity : BaseActivity(), CountrySearchDialogFragment.OnCountrySelectedListener,
    EraseableImageView.OnDrawHistoryChangedListener {

    private lateinit var binding: ActivityNewEpicBinding
    private lateinit var hud: KProgressHUD
    private val countryList by lazy { createCountryList() }
    private var isEraserMode = false

    // EpicActivity থেকে আনা নতুন ভ্যারিয়েবল
    private var selectedImageView: ImageView? = null
    private val sliderHideHandler = Handler(Looper.getMainLooper())
    private lateinit var hideSliderRunnable: Runnable

    private val flagPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadFlagIntoImageViews(it) }
    }

    private val playerImagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.previewPlayerImage1.loadImage(it) {
                resetImageViewScale(binding.previewPlayerImage1) // রিসেট স্কেল
                Toast.makeText(this, "Image 1 loaded. Tap to move/scale.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val playerImagePickerLauncher1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).into(binding.previewPlayerImage2)
            resetImageViewScale(binding.previewPlayerImage2) // রিসেট স্কেল
            Toast.makeText(this, "Image 2 loaded. Tap to move/scale.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewEpicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("Card Creating...").setDetailsLabel("Please wait a moment")
            .setCancellable(false).setDimAmount(0.5f)

        setupToolbar(
            toolbarId = R.id.potw_toolbar,
            title = "New Epic Editor",
            showBackButton = true,
        )

        setupInitialState() // নতুন ফাংশন কল
        binding.previewPlayerImage1.historyChangedListener = this
        setupInteractiveUI()
        resetToDefaults()
    }

    // নতুন ফাংশন
    private fun setupInitialState() {
        hideSliderRunnable = Runnable {
            binding.controls.imageSizeSlider.visibility = View.GONE
            binding.controls.imageSizeLbl.visibility = View.GONE
        }
    }
    
    override fun onHistoryChanged(canUndo: Boolean, canRedo: Boolean) {
        binding.controls.btnUndo.isEnabled = canUndo
        binding.controls.btnRedo.isEnabled = canRedo
    }

    private fun setupInteractiveUI() {
        // TextWatchers and Spinners... (No changes here)
        binding.controls.inputPlayerName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.previewPlayerName.text = if (s.isNullOrEmpty()) getString(R.string.preview_default_player_name) else s
            }
        })
        binding.controls.inputRating.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.previewRatingVertical.text = if (s.isNullOrEmpty()) getString(R.string.previewRating) else s
            }
        })
        binding.controls.positionSpinner.setOnClickListener { showPositionSelectionDialog() }
        binding.controls.countryClubSpinner.setOnClickListener { showCountrySearchDialog() }

        // Button clicks... (No changes here)
        binding.controls.btnUploadLogo.setOnClickListener { flagPickerLauncher.launch("image/*") }
        binding.controls.btnAddPlayerImage1.setOnClickListener { playerImagePickerLauncher1.launch("image/*") }
        binding.controls.btnAddPlayerImage2.setOnClickListener { playerImagePickerLauncher2.launch("image/*") }
        binding.controls.btnResetCard.setOnClickListener { resetToDefaults() }
        binding.controls.btnDownloadCard.setOnClickListener { downloadCard() }

        // Eraser controls... (No changes here)
        binding.controls.btnToggleEraser.setOnClickListener { toggleEraserMode() }
        binding.controls.btnUndo.setOnClickListener { binding.previewPlayerImage1.undo() }
        binding.controls.btnRedo.setOnClickListener { binding.previewPlayerImage1.redo() }
        binding.controls.eraserSizeSlider.addOnChangeListener { _, value, _ -> binding.previewPlayerImage1.eraserSize = value }
        binding.controls.eraserHardnessSlider.addOnChangeListener { _, value, _ -> binding.previewPlayerImage1.eraserHardness = value / 100f }

        // নতুন ফাংশন কল
        setupDraggableImageViews()
        setupImageSizeSlider()
    }

    // নতুন ফাংশন: ছবি ড্র্যাগ এবং স্লাইডার দেখানোর জন্য
    private fun setupDraggableImageViews() {
        var dX = 0f
        var dY = 0f
        val touchListener = View.OnTouchListener { view, event ->
            val imageView = view as ImageView
            if (imageView.drawable == null) return@OnTouchListener false

            // যদি ইরেজার মোড চালু থাকে এবং প্রথম ছবিতে টাচ করা হয়, তবে এই লিসেনার কাজ করবে না
            if (isEraserMode && (view.id == R.id.preview_player_image_1 || view.id == R.id.preview_player_image_2)) {
    return@OnTouchListener false // EraseableImageView কে তার কাজ করতে দেওয়া হচ্ছে
}

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selectedImageView = imageView
                    binding.controls.imageSizeSlider.visibility = View.VISIBLE
                    binding.controls.imageSizeLbl.visibility = View.VISIBLE
                    binding.controls.imageSizeSlider.value = selectedImageView!!.scaleX * 100f
                    resetSliderHideTimer()
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    true
                }
                else -> false
            }
        }
        binding.previewPlayerImage1.setOnTouchListener(touchListener)
        binding.previewPlayerImage2.setOnTouchListener(touchListener)
    }

    // নতুন ফাংশন: স্লাইডার দিয়ে ছবির সাইজ পরিবর্তন
    private fun setupImageSizeSlider() {
        binding.controls.imageSizeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val scale = value / 100.0f
                selectedImageView?.let { view ->
                    view.scaleX = scale
                    view.scaleY = scale
                }
                resetSliderHideTimer()
            }
        }
    }

    // নতুন ফাংশন: স্লাইডার অটো-হাইড করার টাইমার রিসেট
    private fun resetSliderHideTimer() {
        sliderHideHandler.removeCallbacks(hideSliderRunnable)
        sliderHideHandler.postDelayed(hideSliderRunnable, 3000L) // 3 সেকেন্ড পর হাইড হবে
    }

    // পরিবর্তিত ফাংশন
    private fun toggleEraserMode() {
        isEraserMode = !isEraserMode
        binding.previewPlayerImage1.isEraserEnabled = isEraserMode

        val visibility = if (isEraserMode) View.VISIBLE else View.GONE
        binding.controls.eraserSizeLbl.visibility = visibility
        binding.controls.eraserSizeSlider.visibility = visibility
        binding.controls.eraserHardnessLbl.visibility = visibility
        binding.controls.eraserHardnessSlider.visibility = visibility
        
        // ইরেজার মোড চালু হলে সাইজ স্লাইডার হাইড করা হবে
        if(isEraserMode) {
             binding.controls.imageSizeSlider.visibility = View.GONE
             binding.controls.imageSizeLbl.visibility = View.GONE
        }

        val message = if (isEraserMode) "Eraser Mode ON" else "Eraser Mode OFF"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // পরিবর্তিত ফাংশন
    private fun resetToDefaults() {
        binding.previewPlayerImage1.setImageDrawable(null)
        binding.previewPlayerImage1.clearHistory()
        resetImageViewScale(binding.previewPlayerImage1) // রিসেট স্কেল
        
        binding.previewPlayerImage2.setImageDrawable(null)
        resetImageViewScale(binding.previewPlayerImage2) // রিসেট স্কেল

        isEraserMode = false
        binding.previewPlayerImage1.isEraserEnabled = false

        // Resetting text inputs and previews
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

        // Resetting sliders
        binding.controls.eraserSizeLbl.visibility = View.GONE
        binding.controls.eraserSizeSlider.visibility = View.GONE
        binding.controls.eraserHardnessLbl.visibility = View.GONE
        binding.controls.eraserHardnessSlider.visibility = View.GONE
        binding.controls.eraserSizeSlider.value = 20f
        binding.previewPlayerImage1.eraserSize = 20f
        binding.controls.eraserHardnessSlider.value = 100f
        binding.previewPlayerImage1.eraserHardness = 1.0f
        
        // রিসেট করার সময় সাইজ স্লাইডারও হাইড করা হবে
        binding.controls.imageSizeSlider.visibility = View.GONE
        binding.controls.imageSizeLbl.visibility = View.GONE
        binding.controls.imageSizeSlider.value = 100f
    }
    
    // নতুন ফাংশন
    private fun resetImageViewScale(imageView: ImageView) {
        imageView.x = 0f // X-position reset
        imageView.y = 0f // Y-position reset
        imageView.scaleX = 1.0f
        imageView.scaleY = 1.0f
    }

    // --- The rest of the functions (downloadCard, captureCardAsBitmap, etc.) remain unchanged ---
    // --- (নিচের ফাংশনগুলোতে কোনো পরিবর্তন নেই) ---

    private fun downloadCard() {
        hud.show()
        binding.controls.btnDownloadCard.isEnabled = false
        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.Default) { captureCardAsBitmap() }
                val imageUri = withContext(Dispatchers.IO) { saveBitmapToDownloads(bitmap) }
                releaseBitmap(bitmap)
                imageUri?.let { uri ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share Card Via"))
                }
            } finally {
                hud.dismiss()
                binding.controls.btnDownloadCard.isEnabled = true
            }
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

    private suspend fun saveBitmapToDownloads(bitmap: Bitmap): Uri? {
        val filename = "EFCARD_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + "EFCARDIFY")
            }
        }
        return contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
            try {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewEpicActivity, getString(R.string.card_saved_success), Toast.LENGTH_SHORT).show()
                }
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewEpicActivity, getString(R.string.card_saved_failed), Toast.LENGTH_SHORT).show()
                }
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