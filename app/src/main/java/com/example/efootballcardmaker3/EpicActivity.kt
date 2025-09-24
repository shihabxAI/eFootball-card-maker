// ফাইল: EpicActivity.kt (সংশোধিত এবং সম্পূর্ণ)
package com.example.efootballcardmaker3

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.efootballcardmaker3.databinding.ActivityEpicBinding
import com.example.efootballcardmaker3.databinding.LayoutCardEditorControlsBinding

class EpicActivity : BaseEditorActivity<EpicViewModel, ActivityEpicBinding>() {

    override val viewModel: EpicViewModel by viewModels()
    override val controls: LayoutCardEditorControlsBinding by lazy { binding.controls }
    override val cardPreviewContainer: View by lazy { binding.cardPreviewContainer }
    override val coinBalanceTextView: TextView by lazy { binding.epicToolbar.coinBalanceText }
    override val imgFlag: ImageView by lazy { binding.imgFlag }
    override val imgFlagStroke: ImageView by lazy { binding.imgFlagStroke }

    private var selectedImageView: ImageView? = null
    private val sliderHideHandler = Handler(Looper.getMainLooper())
    private lateinit var hideSliderRunnable: Runnable

    private val flagPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onFlagSelected(it) }
    }
    private val playerImagePickerLauncher1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.previewPlayerImage2) }
    }
    private val playerImagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.previewPlayerImage1) }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityEpicBinding = ActivityEpicBinding.inflate(inflater)
    override fun getCardType(): CardType = CardType.EPIC
    override fun getToolbarId(): Int = R.id.epic_toolbar
    override fun getToolbarTitle(): String = "Epic Card Editor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Epic Card এডিটরের জন্য অপ্রয়োজনীয় বাটনগুলো GONE করে দেওয়া হচ্ছে
        controls.btnUndo.visibility = View.GONE
        controls.btnRedo.visibility = View.GONE
        controls.btnToggleEraser.visibility = View.GONE
        controls.eraserSizeSlider.visibility = View.GONE
        controls.eraserSizeLbl.visibility = View.GONE
        controls.eraserHardnessSlider.visibility = View.GONE
        controls.eraserHardnessLbl.visibility = View.GONE
    }

    override fun setupFeatureListeners() {
        controls.btnUploadLogo.setOnClickListener { flagPickerLauncher.launch("image/*") }
        controls.btnAddPlayerImage1.setOnClickListener { playerImagePickerLauncher1.launch("image/*") }
        controls.btnAddPlayerImage2.setOnClickListener { playerImagePickerLauncher2.launch("image/*") }

        controls.btnResetCard.setOnClickListener {
            viewModel.onReset()
            binding.previewPlayerImage1.setImageDrawable(null)
            binding.previewPlayerImage2.setImageDrawable(null)
            resetImageViewScale(binding.previewPlayerImage1)
            resetImageViewScale(binding.previewPlayerImage2)
        }
        
        // ছবি ড্র্যাগ এবং রিসাইজ করার লজিক সেটআপ
        setupDraggableImageViews()
    }

    override fun updatePreview(state: EditorUiState) {
        binding.previewPlayerName.text = state.playerName
        binding.previewRatingVertical.text = state.rating
        binding.previewPositionVertical.text = state.position
        loadFlag(state.flagUrl)
    }

    private fun resetImageViewScale(imageView: ImageView) {
        imageView.scaleX = 1.0f
        imageView.scaleY = 1.0f
    }

    private fun setupDraggableImageViews() {
        hideSliderRunnable = Runnable {
            controls.imageSizeSlider.visibility = View.GONE
            controls.imageSizeLbl.visibility = View.GONE
        }

        var dX = 0f
        var dY = 0f
        val touchListener = View.OnTouchListener { view, event ->
            val imageView = view as ImageView
            if (imageView.drawable == null) return@OnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selectedImageView = imageView
                    controls.imageSizeSlider.visibility = View.VISIBLE
                    controls.imageSizeLbl.visibility = View.VISIBLE
                    controls.imageSizeSlider.value = selectedImageView!!.scaleX * 100f
                    sliderHideHandler.removeCallbacks(hideSliderRunnable)
                    sliderHideHandler.postDelayed(hideSliderRunnable, 3000L)
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.y = event.rawY + dY
                    view.x = event.rawX + dX
                    return@OnTouchListener true
                }
                else -> return@OnTouchListener false
            }
        }
        binding.previewPlayerImage1.setOnTouchListener(touchListener)
        binding.previewPlayerImage2.setOnTouchListener(touchListener)

        controls.imageSizeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val scale = value / 100.0f
                selectedImageView?.let {
                    it.scaleX = scale
                    it.scaleY = scale
                }
                sliderHideHandler.removeCallbacks(hideSliderRunnable)
                sliderHideHandler.postDelayed(hideSliderRunnable, 3000L)
            }
        }
    }
}