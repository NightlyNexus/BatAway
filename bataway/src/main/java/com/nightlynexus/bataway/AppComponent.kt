package com.nightlynexus.bataway

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule

@AppScope
@Component(
  modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    CrashReporterModule::class
  ]
)
internal interface AppComponent {
  fun inject(batAwayApplication: BatAwayApplication)

  fun inject(batAwayActivity: BatAwayActivity)

  fun inject(batAwayListenerService: BatAwayListenerService)

  fun inject(adNotificationListView: AdNotificationListView)

  @Component.Factory
  interface Factory {
    fun create(@BindsInstance application: Application): AppComponent
  }
}

internal fun Application.createAppComponent() = DaggerAppComponent.factory()
  .create(this)
