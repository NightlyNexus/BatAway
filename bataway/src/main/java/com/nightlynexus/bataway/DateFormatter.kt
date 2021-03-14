package com.nightlynexus.bataway

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class DateFormatter(
  timeZone: TimeZone,
  locale: Locale,
  private val use24HourFormat: Boolean,
  now: Date? = null // For testing.
) {
  private val today = Calendar.getInstance(timeZone, locale)
    .apply {
      if (now != null) time = now
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    .timeInMillis
  private val pastYear = Calendar.getInstance(timeZone, locale)
    .apply {
      if (now != null) time = now
      val month = get(Calendar.MONTH)
      if (month == Calendar.DECEMBER) {
        set(Calendar.MONTH, Calendar.JANUARY)
      } else {
        set(Calendar.YEAR, get(Calendar.YEAR) - 1)
        set(Calendar.MONTH, month + 1)
      }
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    .timeInMillis
  private val todayFormat = SimpleDateFormat("HH:mm", Locale.US)
  private val pastYearFormat = SimpleDateFormat("MMM d HH:mm", Locale.US)
  private val olderThanAYearFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US)
  private val todayFormat12Hour = SimpleDateFormat("h:mm a", Locale.US)
  private val pastYearFormat12Hour = SimpleDateFormat("MMM d h:mm a", Locale.US)
  private val olderThanAYearFormat12Hour = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)

  fun format(date: Date): String {
    val time = date.time
    return when {
      time >= today -> {
        if (use24HourFormat) todayFormat else todayFormat12Hour
      }
      time >= pastYear -> {
        if (use24HourFormat) pastYearFormat else pastYearFormat12Hour
      }
      else -> {
        if (use24HourFormat) olderThanAYearFormat else olderThanAYearFormat12Hour
      }
    }.format(date)
  }
}
