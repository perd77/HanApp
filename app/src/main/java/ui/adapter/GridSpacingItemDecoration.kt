package com.mmcl.hanapp.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Adds even spacing around every grid cell so cards never touch each other or the screen edge.
// Applied once to the RecyclerView instead of hardcoding margins into each card layout,
// which keeps the gaps uniform between columns, rows, and outer edges.
class GridSpacingItemDecoration(
    private val spanCount: Int,   // how many columns the grid has (e.g. 2)
    private val spacingPx: Int    // desired gap between cards, in pixels
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Position of this card in the full list, and which column it sits in.
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        val column = position % spanCount

        // Distribute spacing so every card gets an equal share on each side.
        // This math keeps all cells the same width — a naive "add margin" approach
        // would make edge cards wider than middle ones.
        outRect.left = spacingPx - column * spacingPx / spanCount
        outRect.right = (column + 1) * spacingPx / spanCount

        // Add top spacing to every row except the first, so rows are evenly separated.
        if (position >= spanCount) {
            outRect.top = spacingPx
        }
    }
}