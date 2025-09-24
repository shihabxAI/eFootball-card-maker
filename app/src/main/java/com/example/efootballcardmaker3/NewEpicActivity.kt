// ফাইল: NewEpicActivity.kt (সংশোধিত এবং সম্পূর্ণ)
package com.example.efootballcardmaker3

import android.net.Uri
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
import com.example.efootballcardmaker3.databinding.ActivityNewEpicBinding
import com.example.efootballcardmaker3.databinding.LayoutCardEditorControlsBinding

class NewEpicActivity : BaseEditorActivity<NewEpicViewModel, ActivityNewEpicBinding>(), EraseableImageView.OnDrawHistoryChangedListener {

    override val viewModel: NewEpicViewModel by viewModels()
    override val controls: LayoutCardEditorControlsBinding by lazy { binding.controls }
    override val cardPreviewContainer: View by lazy { binding.cardPreviewContainer }
    override val coinBalanceTextView: TextView by lazy { binding.potwToolbar.coinBalanceText }
    override val imgFlag: ImageView by lazy { binding.imgFlag }
    override val imgFlagStroke: ImageView by lazy { binding.imgFlagStroke }
    
    private var selectedImageView: ImageView? = null
    private val sliderHideHandler = Handler(Looper.getMainLooper())
    private lateinit var hideSliderRunnable: Runnable
    private var isEraserMode = false

    private val flagPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onFlagSelected(it) }
    }
    private val playerImagePickerLauncher1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.previewPlayerImage2) }
    }
    private val playerImagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { binding.previewPlayerImage1.loadImage(it) {} }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityNewEpicBinding = ActivityNewEpicBinding.inflate(inflater)
    override fun getCardType(): CardType = CardType.NEW_EPIC
    override fun getToolbarId(): Int = R.id.potw_toolbar
    override fun getToolbarTitle(): String = "New Epic Editor"

    override fun setupFeatureListeners() {
        binding.previewPlayerImage1.historyChangedListener = this

        controls.btnUploadLogo.setOnClickListener { flagPickerLauncher.launch("image/*") }
        controls.btnAddPlayerImage1.setOnClickListener { playerImagePickerLauncher1.launch("image/*") }
        controls.btnAddPlayerImage2.setOnClickListener { playerImagePickerLauncher2.launch("image/*") }

        controls.btnToggleEraser.setOnClickListener {
            isEraserMode = !isEraserMode
            binding.previewPlayerImage1.isEraserEnabled = isEraserMode
            updateEraserUi(isEraserMode)
        }
        controls.btnUndo.setOnClickListener { binding.previewPlayerImage1.undo() }
        controls.btnRedo.setOnClickListener { binding.previewPlayerImage1.redo() }

        controls.eraserSizeSlider.addOnChangeListener { _, value, _ -> binding.previewPlayerImage1.eraserSize = value }
        controls.eraserHardnessSlider.addOnChangeListener { _, value, _ -> binding.previewPlayerImage1.eraserHardness = value / 100f }

        controls.btnResetCard.setOnClickListener {
            viewModel.onReset()
            binding.previewPlayerImage1.clearHistory()
            binding.previewPlayerImage1.setImageDrawable(null)
            binding.previewPlayerImage2.setImageDrawable(null)
            isEraserMode = false
            updateEraserUi(false)
        }
        setupDraggableImageViews()
    }
    
    override fun updatePreview(state: EditorUiState) {
        binding.previewPlayerName.text = state.playerName
        binding.previewRatingVertical.text = state.rating
        binding.previewPositionVertical.text = state.position
        loadFlag(state.flagUrl)
    }

    override fun onHistoryChanged(canUndo: Boolean, canRedo: Boolean) {
        controls.btnUndo.isEnabled = canUndo
        controls.btnRedo.isEnabled = canRedo
    }

    private fun updateEraserUi(isEraserOn: Boolean) {
        val visibility = if (isEraserOn) View.VISIBLE else View.GONE
        controls.eraserSizeLbl.visibility = visibility
        controls.eraserSizeSlider.visibility = visibility
        controls.eraserHardnessLbl.visibility = visibility
        controls.eraserHardnessSlider.visibility = visibility
        
        if (isEraserOn) {
            controls.imageSizeSlider.visibility = View.GONE
            controls.imageSizeLbl.visibility = View.GONE
        }
    }

    private fun setupDraggableImageViews() {
        hideSliderRunnable = Runnable {
            controls.imageSizeSlider.visibility = View.GONE
            controls.imageSizeLbl.visibility = View.GONE
        }

        var dX = 0f
        var dY = 0f
        val touchListener = View.OnTouchListener { view, event ->
            if (isEraserMode) {
                return@OnTouchListener false
            }
            
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