package com.nightlynexus.bataway

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.AttributeSet
import android.view.View.MeasureSpec.EXACTLY
import android.view.WindowInsets
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private class ExtendedToolbar(
  context: Context,
  attributes: AttributeSet
) : Toolbar(context, attributes) {
  private var extraHeight = 0
  private var initialHeight = 0
  private var initialMeasure = true

  init {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
      val systemBarsAndCutout = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
      )
      val cutout = insets.getInsets(
        WindowInsetsCompat.Type.displayCutout()
      )
      extraHeight = systemBarsAndCutout.top
      println("initialPadding: ${paddingTop} ${paddingLeft} ${paddingBottom} ${paddingRight}")
      v.setPadding(
        cutout.left,
        systemBarsAndCutout.top,
        cutout.right,
        0
      )
      insets
    }
  }

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    if (initialMeasure) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
      initialHeight = measuredHeight
      initialMeasure = false
    } else {
      super.onMeasure(
        widthMeasureSpec, MeasureSpec.makeMeasureSpec(initialHeight + extraHeight, EXACTLY)
      )
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    requestApplyInsets()
  }
}
