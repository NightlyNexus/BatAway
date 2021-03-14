package com.nightlynexus.bataway;

import javax.inject.Inject

@AppScope
internal class NotificationIdProvider @Inject constructor() {
  var notificationId = 0
    get() = ++field
    private set
}
