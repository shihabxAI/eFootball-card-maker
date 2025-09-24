// ফাইল: BaseEditorActivity.kt (সংশোধিত এবং সম্পূর্ণ)
package com.example.efootballcardmaker3

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView // <-- প্রয়োজনীয় ইমপোর্ট যোগ করা হয়েছে
import android.widget.TextView // <-- প্রয়োজনীয় ইমপোর্ট যোগ করা হয়েছে
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.efootballcardmaker3.data.DataSource
import com.example.efootballcardmaker3.databinding.LayoutCardEditorControlsBinding
import io.github.rupinderjeet.kprogresshud.KProgressHUD
import kotlinx.coroutines.launch

abstract class BaseEditorActivity<VM : BaseEditorViewModel, VB : ViewBinding> : BaseActivity() {

    protected lateinit var binding: VB
    protected lateinit var hud: KProgressHUD
    protected abstract val viewModel: VM

    // Child Activity থেকে ViewBinding এর মাধ্যমে View গুলো নিরাপদে পাওয়ার জন্য abstract property
    protected abstract val controls: LayoutCardEditorControlsBinding
    protected abstract val cardPreviewContainer: View
    protected abstract val coinBalanceTextView: TextView
    protected abstract val imgFlag: ImageView
    protected abstract val imgFlagStroke: ImageView

    private val countryList by lazy { DataSource.getCountryList() }

    // Child Activity-কে অবশ্যই এই মেথডগুলো ইমপ্লিমেন্ট করতে হবে
    protected abstract fun inflateBinding(inflater: LayoutInflater): VB
    protected abstract fun getCardType(): CardType
    protected abstract fun getToolbarId(): Int
    protected abstract fun getToolbarTitle(): String

    // ছবি এবং অন্যান্য ফিচার-সম্পর্কিত লিসেনার সেটআপ করার জন্য
    protected abstract fun setupFeatureListeners()
    // UI স্টেট অনুযায়ী প্রিভিউ আপডেট করার জন্য
    protected abstract fun updatePreview(state: EditorUiState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)

        viewModel.loadInitialCoins(this, getCardType())
        setupHud()
        setupToolbar(getToolbarId(), getToolbarTitle(), true)
        
        setupCommonListeners()
        setupFeatureListeners() // ফিচার-স্পেসিফিক লিসেনার সেটআপ
        observeViewModel()
        setupCountrySelectionListener()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> updateUi(state) } }
                launch { viewModel.events.collect { event -> handleEvent(event) } }
                launch {
                    viewModel.coinBalance.collect { balance ->
                        coinBalanceTextView.text = balance.toString()
                    }
                }
            }
        }
    }

    private fun updateUi(state: EditorUiState) {
        // কমন কন্ট্রোলগুলো আপডেট করা
        controls.positionSpinner.text = state.position
        controls.countryClubSpinner.text = state.countryName
        controls.positionSpinner.setTextColor(ContextCompat.getColor(this, state.positionTextColorRes))
        controls.countryClubSpinner.setTextColor(ContextCompat.getColor(this, state.countryTextColorRes))
        
        // চাইল্ড-স্পেসিফিক প্রিভিউ আপডেট করা
        updatePreview(state)
    }
    
    private fun setupCommonListeners() {
        controls.inputPlayerName.addTextChangedListener { viewModel.onPlayerNameChanged(it) }
        controls.inputRating.addTextChangedListener { viewModel.onRatingChanged(it) }
        controls.positionSpinner.setOnClickListener { showPositionSelectionDialog() }
        controls.countryClubSpinner.setOnClickListener { showCountrySearchDialog() }
        
        controls.btnDownloadCard.setOnClickListener {
            val bitmap = captureCardAsBitmap()
            viewModel.onDownloadCard(this, bitmap, getCardType())
        }
    }

    private fun setupCountrySelectionListener() {
        supportFragmentManager.setFragmentResultListener(
            CountrySearchDialogFragment.REQUEST_KEY,
            this
        ) { _, bundle ->
            val selectedCountry: Country? = if (Build.VERSION.SDK_INT >= 33) {
                bundle.getParcelable(CountrySearchDialogFragment.KEY_COUNTRY, Country::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(CountrySearchDialogFragment.KEY_COUNTRY)
            }
            selectedCountry?.let { viewModel.onCountrySelected(it) }
        }
    }

    private fun handleEvent(event: BaseEditorViewModel.Event?) {
        when (event) {
            is BaseEditorViewModel.Event.ShowToast -> Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            is BaseEditorViewModel.Event.ShareCard -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, event.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Share Card Via"))
            }
            is BaseEditorViewModel.Event.ShowLoading -> hud.setLabel(event.message).show()
            is BaseEditorViewModel.Event.HideLoading -> hud.dismiss()
            null -> {}
        }
        viewModel.consumeEvent()
    }

    private fun captureCardAsBitmap(): Bitmap {
        val view = cardPreviewContainer
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    
    private fun showPositionSelectionDialog() {
        val positions = resources.getStringArray(R.array.player_positions)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_position))
            .setItems(positions) { dialog, which -> 
                viewModel.onPositionSelected(positions[which])
                dialog.dismiss() 
            }
            .show()
    }
    
    private fun showCountrySearchDialog() {
        CountrySearchDialogFragment(countryList)
            .show(supportFragmentManager, CountrySearchDialogFragment.TAG)
    }
    
    private fun setupHud() {
        hud = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("Card Creating...")
            .setDetailsLabel("Please wait a moment")
            .setCancellable(false)
            .setDimAmount(0.5f)
    }
    
    protected fun loadFlag(flagUrl: Any?) {
        flagUrl?.let {
            Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgFlag)
            Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgFlagStroke)
        }
    }
}