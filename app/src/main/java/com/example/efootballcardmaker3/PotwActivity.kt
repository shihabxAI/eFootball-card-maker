// ফাইল: PotwActivity.kt
package com.example.efootballcardmaker3

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.efootballcardmaker3.databinding.ActivityPotwBinding
import com.example.efootballcardmaker3.databinding.LayoutCardEditorControlsBinding

class PotwActivity : BaseEditorActivity<PotwViewModel, ActivityPotwBinding>(), EraseableImageView.OnDrawHistoryChangedListener {

    override val viewModel: PotwViewModel by viewModels()
    override val controls: LayoutCardEditorControlsBinding by lazy { binding.controls }
    override val cardPreviewContainer: View by lazy { binding.cardPreviewContainer }
    override val coinBalanceTextView: TextView by lazy { binding.potwToolbar.coinBalanceText }
    override val imgFlag: ImageView by lazy { binding.imgFlag }
    override val imgFlagStroke: ImageView by lazy { binding.imgFlagStroke }


    override fun inflateBinding(inflater: LayoutInflater): ActivityPotwBinding = ActivityPotwBinding.inflate(inflater)
    override fun getCardType(): CardType = CardType.POTW
    override fun getToolbarId(): Int = R.id.potw_toolbar
    override fun getToolbarTitle(): String = "POTW Card Editor"
   // override fun getCoinBalanceTextViewId(): Int = binding.potwToolbar.coinBalanceText.id

    private val flagPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onFlagSelected(it) }
    }
    private val playerImagePickerLauncher1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.previewPlayerImage1.loadImage(it) {
                Toast.makeText(this, "Main image loaded. Tap 'Eraser' to edit.", Toast.LENGTH_LONG).show()
            }
        }
    }
    private val playerImagePickerLauncher2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.previewPlayerImage2)
            Toast.makeText(this, "Background image loaded.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun setupFeatureListeners() {
        binding.previewPlayerImage1.historyChangedListener = this

        controls.btnUploadLogo.setOnClickListener { flagPickerLauncher.launch("image/*") }
        controls.btnAddPlayerImage1.setOnClickListener { playerImagePickerLauncher1.launch("image/*") }
        controls.btnAddPlayerImage2.setOnClickListener { playerImagePickerLauncher2.launch("image/*") }

        controls.btnToggleEraser.setOnClickListener {
            val isCurrentlyEnabled = binding.previewPlayerImage1.isEraserEnabled
            binding.previewPlayerImage1.isEraserEnabled = !isCurrentlyEnabled
            updateEraserUi(!isCurrentlyEnabled)
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
        }
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
    }
}