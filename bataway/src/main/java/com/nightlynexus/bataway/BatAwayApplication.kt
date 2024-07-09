package com.nightlynexus.bataway

import android.app.Application
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class BatAwayApplication : Application(), HasAndroidInjector {
  @Inject internal lateinit var androidInjector: DispatchingAndroidInjector<Any>

  override fun onCreate() {
    createAppComponent()
      .inject(this)
    super.onCreate()
  }

  override fun androidInjector() = androidInjector
}
