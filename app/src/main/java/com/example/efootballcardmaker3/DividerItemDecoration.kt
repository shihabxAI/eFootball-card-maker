package com.example.efootballcardmaker3 // তোমার প্যাকেজ নাম

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(context: Context, private val leftPaddingDp: Int = 0) : RecyclerView.ItemDecoration() {

    private val divider: Drawable?
    private val paddingPx: Int

    init {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val a = context.obtainStyledAttributes(attrs)
        divider = a.getDrawable(0)
        a.recycle()
        
        paddingPx = (leftPaddingDp * context.resources.displayMetrics.density).toInt()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (divider == null) {
            return
        }

        val left = parent.paddingLeft + paddingPx
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}