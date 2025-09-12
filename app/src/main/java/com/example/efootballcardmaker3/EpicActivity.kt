package com.example.efootballcardmaker3

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import io.github.rupinderjeet.kprogresshud.KProgressHUD;
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.efootballcardmaker3.databinding.ActivityEpicBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class EpicActivity : BaseActivity(), CountrySearchDialogFragment.OnCountrySelectedListener {

    private lateinit var binding: ActivityEpicBinding
    private lateinit var hud: KProgressHUD // 👈 এই ভ্যারিয়েবলটি যোগ করুন
    private val countryList by lazy { createCountryList() }
    private var selectedImageView: ImageView? = null
    private val sliderHideHandler = Handler(Looper.getMainLooper())
    private lateinit var hideSliderRunnable: Runnable

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val MAX_SCALE_PLAYER1 = 300f
        private const val MAX_SCALE_PLAYER2 = 300f
    }

    // লঞ্চারগুলো
    private val flagPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadFlagIntoImageViews(it) }
    }

    private val playerImagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.previewPlayerImage1)

            binding.previewPlayerImage1.setColorFilter(
                android.graphics.Color.parseColor("#1000FF00"),
                android.graphics.PorterDuff.Mode.SRC_ATOP
            )
            resetImageViewScale(binding.previewPlayerImage1)
        }
    }

    private val playerImagePickerLauncher1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.previewPlayerImage2)

            resetImageViewScale(binding.previewPlayerImage2)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpicBinding.inflate(layoutInflater)
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
            toolbarId = R.id.epic_toolbar, // আপনার লেআউটের <include> ট্যাগের আইডি
            title = "Epic card editor",
            showBackButton = true,
            //menuResId = R.menu.toolbar_menu // 👈 মেন্যু ফাইল যোগ করা হলো
            //BaseActivity remember
        )

        setupInitialState()
        setupInteractiveUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // coroutine scope cleanup
    }

    private fun setupInitialState() {
   binding.controls.btnToggleEraser.visibility = View.GONE
    binding.controls.btnUndo.visibility = View.GONE
    binding.controls.btnRedo.visibility = View.GONE
        hideSliderRunnable = Runnable {
           

            binding.controls.imageSizeSlider.visibility = View.GONE
            binding.controls.imageSizeLbl.visibility = View.GONE
        }
        resetToDefaults()
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
        binding.controls.btnAddPlayerImage1.setOnClickListener { playerImagePickerLauncher1.launch("image/*") }
        binding.controls.btnAddPlayerImage2.setOnClickListener { playerImagePickerLauncher2.launch("image/*") }

        binding.controls.btnResetCard.setOnClickListener { resetToDefaults() }
        binding.controls.btnDownloadCard.setOnClickListener { downloadCard() }

        setupDraggableImageViews()
        setupImageSizeSlider()
    }

    private fun resetToDefaults() {
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

        binding.previewPlayerImage1.setImageDrawable(null)
        resetImageViewScale(binding.previewPlayerImage1)
        binding.previewPlayerImage2.setImageDrawable(null)
        resetImageViewScale(binding.previewPlayerImage2)

        binding.controls.imageSizeSlider.visibility = View.GONE
        binding.controls.imageSizeLbl.visibility = View.GONE
        binding.controls.imageSizeSlider.value = 100f
    }

    private fun shareCard() {
        val bitmap = captureCardAsBitmap()
        activityScope.launch(Dispatchers.IO) {
            val uri = saveBitmapToCache(bitmap)
            withContext(Dispatchers.Main) {
                if (uri == null) {
                    Toast.makeText(this@EpicActivity, "Failed to create sharable image", Toast.LENGTH_SHORT).show()
                } else {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_card_title)))
                }
            }
            releaseBitmap(bitmap)
        }
    }
private fun downloadCard() {
    // KProgressHUD দেখানো শুরু করুন এবং বাটনটি নিষ্ক্রিয় করুন
    hud.show()
    binding.controls.btnDownloadCard.isEnabled = false

    lifecycleScope.launch {
        try {
            // Bitmap তৈরির কাজটি ব্যাকগ্রাউন্ডে হবে
            val bitmap = withContext(Dispatchers.Default) {
                captureCardAsBitmap()
            }
            
            // ফাইল সেভ করার কাজটিও ব্যাকগ্রাউন্ডে হবে
            val imageUri = withContext(Dispatchers.IO) {
                saveBitmapToDownloads(bitmap)
            }
            
            releaseBitmap(bitmap)

            // UI সম্পর্কিত কাজ (শেয়ার ডায়ালগ) Main Thread-এ হবে
            imageUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Card Via"))
            }
        } finally {
            // কাজ সফল হোক বা ব্যর্থ, সবশেষে HUD লুকানো হবে এবং বাটন সক্রিয় হবে
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

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_card.png")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun saveBitmapToDownloads(bitmap: Bitmap): Uri? {
    val filename = "EFCARD_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + File.separator + "EFCARDIFY"
            )
        }
    }

    // Main thread-এ UI কাজ করার জন্য context পরিবর্তন
    return withContext(Dispatchers.Main) {
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        try {
            uri?.let {
                contentResolver.openOutputStream(it)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
            }
            Toast.makeText(this@EpicActivity, getString(R.string.card_saved_success), Toast.LENGTH_SHORT).show()
            uri // 👈 সফল হলে Uri রিটার্ন করবে
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@EpicActivity, getString(R.string.card_saved_failed), Toast.LENGTH_SHORT).show()
            null // 👈 ব্যর্থ হলে null রিটার্ন করবে
        }
    }
}

    private fun showPositionSelectionDialog() {
        val positions = resources.getStringArray(R.array.player_positions)
        AlertDialog.Builder(this)
            .setTitle("Select Position")
            .setItems(positions) { dialog, which ->
                val selectedPosition = positions[which]
                binding.controls.positionSpinner.text = selectedPosition
                binding.previewPositionVertical.text = selectedPosition
                binding.controls.positionSpinner.setTextColor(ContextCompat.getColor(this, R.color.black))
                dialog.dismiss()
            }
            .show()
    }

    private fun setupDraggableImageViews() {
        var dX = 0f
        var dY = 0f
        val touchListener = View.OnTouchListener { view, event ->
            val imageView = view as ImageView
            if (imageView.drawable == null) return@OnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selectedImageView = imageView
                    when (selectedImageView?.id) {
                        R.id.preview_player_image_1 -> binding.controls.imageSizeSlider.valueTo = MAX_SCALE_PLAYER1
                        R.id.preview_player_image_2 -> binding.controls.imageSizeSlider.valueTo = MAX_SCALE_PLAYER2
                    }
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

    private fun resetSliderHideTimer() {
        sliderHideHandler.removeCallbacks(hideSliderRunnable)
        sliderHideHandler.postDelayed(hideSliderRunnable, 3000L)
    }

    override fun onCountrySelected(country: Country) {
        binding.controls.countryClubSpinner.text = country.name
        binding.controls.countryClubSpinner.setTextColor(ContextCompat.getColor(this, R.color.black))
        loadFlagIntoImageViews(country.flagUrl)
    }

    private fun <T> loadFlagIntoImageViews(model: T) {
        Glide.with(this)
            .load(model)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imgFlag)

        Glide.with(this)
            .load(model)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imgFlagStroke)
    }

    private fun resetImageViewScale(imageView: ImageView) {
        imageView.scaleX = 1.0f
        imageView.scaleY = 1.0f
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