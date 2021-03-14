package com.nightlynexus.bataway

import android.app.Notification
import android.app.PendingIntent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BatAwayListenerService : NotificationListenerService() {
  @Inject internal lateinit var enabledPreference: EnabledPreference
  @Inject internal lateinit var adNotificationQueries: AdNotificationQueries

  @Inject @AdNotificationContentIntents
  internal lateinit var adNotificationContentIntents: MutableMap<String, PendingIntent>

  override fun onCreate() {
    AndroidInjection.inject(this)
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    if (!enabledPreference.enabled) {
      return
    }
    if (sbn.packageName != "com.brave.browser") {
      return
    }
    // Ads channel ids:
    // https://github.com/brave/brave-core/blob/0de7d31a421b212808b3e11000546a1b52c220e5/android/java/org/chromium/chrome/browser/notifications/channels/BraveChannelDefinitions.java#L21
    if (sbn.notification.channelId.contains("com.brave.browser.ads")) {
      val key = sbn.key
      val contentIntent = sbn.notification.contentIntent
      val notificationExtras = sbn.notification.extras
      val message = notificationExtras.getCharSequence(Notification.EXTRA_TEXT)
      val title = notificationExtras.getCharSequence(Notification.EXTRA_TITLE)
      adNotificationContentIntents[key] = contentIntent
      // The deleteIntent is automatically called.
      cancelNotification(key)
      GlobalScope.launch(Dispatchers.IO) {
        adNotificationQueries.insert(key, title.toString(), message.toString(), sbn.postTime)
      }
    }
  }
}
