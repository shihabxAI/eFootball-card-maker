package com.example.efootballcardmaker3

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.ArrayDeque

class EraseableImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // **NEW**: Public property to control erasing from the Activity
    var isEraserEnabled = false

    // ... (listener and other properties remain the same)
    interface OnDrawHistoryChangedListener {
        fun onHistoryChanged(canUndo: Boolean, canRedo: Boolean)
    }
    var historyChangedListener: OnDrawHistoryChangedListener? = null

    private var sourceBitmap: Bitmap? = null
    private var drawingBitmap: Bitmap? = null
    private var drawingCanvas: Canvas? = null
    private var erasePaint: Paint = Paint()

    private val undoStack = ArrayDeque<Path>()
    private val redoStack = ArrayDeque<Path>()
    private var currentPath: Path? = null

    private val reusableMatrixValues = FloatArray(9)
    private val reusableTouchPoint = PointF()
    private var mX: Float = 0f
    private var mY: Float = 0f
    private val touchTolerance = 4f

    var eraserSize: Float = 20f
        set(value) {
            field = value
            updatePaint()
        }

    var eraserHardness: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
            updatePaint()
        }

    init {
        setupEraser()
    }

    private fun setupEraser() {
        erasePaint.apply {
            isAntiAlias = true
            isDither = true
            color = Color.TRANSPARENT
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        updatePaint()
    }
    
    // All other functions (updatePaint, loadImage, undo, redo, clearHistory, redrawAllPaths, getTouchPointOnBitmap, notifyHistoryChanged) remain unchanged.
    // ...

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // **FIX**: Check if the eraser is enabled before processing any touch event.
        if (!isEraserEnabled || drawingBitmap == null) {
            return false // Return false to indicate the event was not handled.
        }

        val mapped = getTouchPointOnBitmap(event.x, event.y) ?: return false
        val x = mapped.x
        val y = mapped.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path().apply {
                    moveTo(x, y)
                }
                mX = x
                mY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - mX)
                val dy = Math.abs(y - mY)
                if (dx >= touchTolerance || dy >= touchTolerance) {
                    currentPath?.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                    mX = x
                    mY = y
                    currentPath?.let { drawingCanvas?.drawPath(it, erasePaint) }
                }
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.let {
                    undoStack.addLast(Path(it))
                    redoStack.clear()
                    notifyHistoryChanged()
                }
                currentPath = null
            }
        }

        invalidate()
        // Return true to consume the touch event so it doesn't propagate further.
        return true
    }
    
    // --- The rest of the functions are unchanged. I've included them for completeness. ---
    
    private fun updatePaint() {
    erasePaint.strokeWidth = eraserSize

    if (eraserHardness >= 1.0f) {
        // Hardness 1 হলে sharp
        erasePaint.maskFilter = null
    } else {
        // Hardness 0 → বেশি blur, Hardness 1 → কম blur
        val baseBlur = 50f                // ছোট ব্রাশ হলেও blur থাকবে
        val sizeFactor = eraserSize * 0.5f // সাইজ বাড়লে blurও কিছুটা বাড়বে
        val maxBlur = baseBlur + sizeFactor

        val blurRadius = maxBlur * (1.0f - eraserHardness)

        erasePaint.maskFilter = if (blurRadius > 0) {
            BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        } else {
            null
        }
    }
}
    fun loadImage(uri: Uri, onImageLoaded: () -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .override(1000, 1000)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    sourceBitmap = resource
                    drawingBitmap = resource.copy(Bitmap.Config.ARGB_8888, true)
                    drawingCanvas = Canvas(drawingBitmap!!)
                    setImageBitmap(drawingBitmap)
                    clearHistory()
                    onImageLoaded()
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    sourceBitmap = null
                    drawingBitmap = null
                    drawingCanvas = null
                }
            })
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastPath = undoStack.removeLast()
            redoStack.addLast(lastPath)
            redrawAllPaths()
            notifyHistoryChanged()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val pathToRedo = redoStack.removeLast()
            undoStack.addLast(pathToRedo)
            redrawAllPaths()
            notifyHistoryChanged()
        }
    }

    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        sourceBitmap?.let {
            drawingBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            drawingCanvas = Canvas(drawingBitmap!!)
            setImageBitmap(drawingBitmap)
        }
        notifyHistoryChanged()
        invalidate()
    }

    private fun redrawAllPaths() {
        sourceBitmap?.let {
            drawingBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            drawingCanvas = Canvas(drawingBitmap!!)
            for (path in undoStack) {
                drawingCanvas?.drawPath(path, erasePaint)
            }
            setImageBitmap(drawingBitmap)
            invalidate()
        }
    }

    private fun getTouchPointOnBitmap(x: Float, y: Float): PointF? {
        drawable ?: return null
        drawingBitmap ?: return null
        imageMatrix.getValues(reusableMatrixValues)
        val scaleX = reusableMatrixValues[Matrix.MSCALE_X]
        val scaleY = reusableMatrixValues[Matrix.MSCALE_Y]
        val transX = reusableMatrixValues[Matrix.MTRANS_X]
        val transY = reusableMatrixValues[Matrix.MTRANS_Y]
        if (scaleX == 0f || scaleY == 0f) return null
        val bmpX = (x - transX) / scaleX
        val bmpY = (y - transY) / scaleY
        if (bmpX < 0 || bmpY < 0 || bmpX > drawingBitmap!!.width || bmpY > drawingBitmap!!.height) {
            return null
        }
        reusableTouchPoint.set(bmpX, bmpY)
        return reusableTouchPoint
    }
    
    private fun notifyHistoryChanged() {
        historyChangedListener?.onHistoryChanged(undoStack.isNotEmpty(), redoStack.isNotEmpty())
    }
}
