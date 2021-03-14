package com.nightlynexus.bataway

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater

private const val APP_COMPONENT = "com.nightlynexus.bataway.APP_COMPONENT"

internal val Context.appComponent
  @SuppressLint("WrongConstant") get() = getSystemService(APP_COMPONENT) as AppComponent

internal fun Context.withAppComponent(appComponent: AppComponent): Context =
  CustomServiceContextWrapper(this, APP_COMPONENT, appComponent)

private class CustomServiceContextWrapper(
  context: Context,
  private val serviceName: String,
  private val service: Any
) : LayoutInflaterCloningContextWrapper(context) {
  override fun getSystemService(name: String): Any? {
    if (name == serviceName) return service
    return super.getSystemService(name)
  }
}

private open class LayoutInflaterCloningContextWrapper(context: Context) : ContextWrapper(context) {
  private var inflater: LayoutInflater? = null

  override fun getSystemService(name: String): Any? {
    if (name == Context.LAYOUT_INFLATER_SERVICE) {
      if (inflater == null) {
        inflater = LayoutInflater.from(baseContext)
          .cloneInContext(this)
      }
      return inflater
    }
    return super.getSystemService(name)
  }
}
