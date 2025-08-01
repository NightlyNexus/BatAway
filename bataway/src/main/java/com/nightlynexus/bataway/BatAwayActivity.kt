package com.nightlynexus.bataway

import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.sqldelight.Query
import dagger.android.AndroidInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BatAwayActivity : AppCompatActivity() {
  @Inject internal lateinit var appComponent: AppComponent
  @Inject internal lateinit var enabledPreference: EnabledPreference
  @Inject internal lateinit var adNotificationQueries: AdNotificationQueries
  private lateinit var scope: CoroutineScope
  private lateinit var toolbar: Toolbar
  private lateinit var switch: CompoundButton
  private lateinit var switchText: TextView
  private lateinit var adsBlocked: TextView
  private lateinit var adsBlockedCount: Query<Long>
  private lateinit var countListener: Query.Listener

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    scope = MainScope()
    val inflater = LayoutInflater.from(withAppComponent(appComponent))
    inflater.inflate(R.layout.activity, findViewById(android.R.id.content), true)
    val root = findViewById<ViewGroup>(R.id.root)
    toolbar = root.findViewById(R.id.toolbar)
    switch = root.findViewById(R.id.enable_switch)
    switchText = root.findViewById(R.id.enable_switch_text)
    adsBlocked = root.findViewById(R.id.ads_blocked)
    val listShadow = root.findViewById<View>(R.id.list_shadow)
    val list = root.findViewById<View>(R.id.list)
    if (hasNotificationPermission && enabledPreference.enabled) {
      switch.isChecked = true
      switchText.setText(R.string.switch_enabled)
    } else {
      switch.isChecked = false
      switchText.setText(R.string.switch_disabled)
    }
    switch.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked && !hasNotificationPermission) {
        switch.isChecked = false
        launchNotificationPermissionSettings()
        return@setOnCheckedChangeListener
      }
      enabledPreference.enabled = isChecked
      if (isChecked) {
        switchText.setText(R.string.switch_enabled)
      } else {
        switchText.setText(R.string.switch_disabled)
      }
    }
    scope.launch(Dispatchers.IO) {
      val count = adNotificationQueries.count().executeAsOne().toInt()
      withContext(Dispatchers.Main) {
        if (count == 0) {
          adsBlocked.visibility = View.GONE
          listShadow.visibility = View.GONE
        } else {
          adsBlocked.visibility = View.VISIBLE
          adsBlocked.text = resources.getQuantityString(R.plurals.ads_blocked, count, count)
          listShadow.visibility = View.VISIBLE
        }
      }
    }
    adsBlockedCount = adNotificationQueries.count()
    countListener = object : Query.Listener {
      override fun queryResultsChanged() {
        val count = adNotificationQueries.count().executeAsOne().toInt()
        scope.launch(Dispatchers.Main) {
          if (count == 0) {
            adsBlocked.visibility = View.GONE
            listShadow.visibility = View.GONE
          } else {
            adsBlocked.visibility = View.VISIBLE
            adsBlocked.text = resources.getQuantityString(R.plurals.ads_blocked, count, count)
            listShadow.visibility = View.VISIBLE
          }
        }
      }
    }
    adsBlockedCount.addListener(countListener)
    toolbar.inflateMenu(R.menu.toolbar)
    toolbar.setOnMenuItemClickListener {
      showContributeDialog()
      true
    }
    ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(
        systemBars.left,
        0,
        systemBars.right,
        0
      )
      insets
    }
    // Don't handle the Toolbar or the RecyclerView here.
    for (i in 1 until root.childCount - 1) {
      val child = root.getChildAt(i)
      ViewCompat.setOnApplyWindowInsetsListener(child) { v, insets ->
        val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        v.setPadding(
          cutout.left,
          0,
          cutout.right,
          0
        )
        insets
      }
    }
    ViewCompat.setOnApplyWindowInsetsListener(list) { v, insets ->
      val systemBarsAndCutout = insets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
      )
      val cutout = insets.getInsets(
        WindowInsetsCompat.Type.displayCutout()
      )
      v.setPadding(
        cutout.left,
        0,
        cutout.right,
        systemBarsAndCutout.bottom
      )
      insets
    }
  }

  override fun onResume() {
    super.onResume()
    if (!hasNotificationPermission) {
      switch.isChecked = false
    }
  }

  private val hasNotificationPermission
    get() = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

  private fun launchNotificationPermissionSettings() {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    val hasSettingsActivity = intent.resolveActivity(packageManager) != null
    Toast.makeText(this, R.string.toast_enable_in_settings, Toast.LENGTH_LONG).show()
    if (hasSettingsActivity) {
      startActivityForResult(intent, 1)
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (hasNotificationPermission) {
      switch.isChecked = true
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    adsBlockedCount.removeListener(countListener)
    scope.cancel()
  }

  private fun showContributeDialog() {
    Dialog(this).apply {
      setContentView(R.layout.contribute)
      window!!.setBackgroundDrawableResource(R.color.contribute_background)
      findViewById<View>(R.id.contribute_code).setOnClickListener {
        startActivity(
          Intent(ACTION_VIEW, Uri.parse("https://github.com/NightlyNexus/BatAway"))
        )
      }
    }.show()
  }
}
