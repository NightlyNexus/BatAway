package com.nightlynexus.bataway

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State

internal class SpaceItemDecoration(@Px private val space: Int) : ItemDecoration() {
  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: State
  ) {
    outRect.set(0, if (parent.getChildLayoutPosition(view) == 0) space else 0, 0, space)
  }
}
