package com.nightlynexus.bataway

import android.content.SharedPreferences

internal class EnabledPreference(private val preference: SharedPreferences) {
  @Volatile var enabled = preference.getBoolean("enabled", true)
    set(value) {
      field = value
      preference.edit().putBoolean("enabled", value).apply()
    }
}
