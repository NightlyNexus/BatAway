package com.nightlynexus.bataway

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DECEMBER
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.JANUARY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.YEAR
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * DateFormatter formats dates and times differently depending on how old they are.
 */
internal class DateFormatter(
  timeZone: TimeZone,
  locale: Locale,
  private val use24HourFormat: Boolean,
  now: Date? = null // For testing.
) {
  private val today = Calendar.getInstance(timeZone, locale)
    .apply {
      if (now != null) time = now
      set(HOUR_OF_DAY, 0)
      set(MINUTE, 0)
      set(SECOND, 0)
      set(MILLISECOND, 0)
    }
    .timeInMillis
  private val pastYear = Calendar.getInstance(timeZone, locale)
    .apply {
      if (now != null) time = now
      val month = get(MONTH)
      if (month == DECEMBER) {
        set(MONTH, JANUARY)
      } else {
        set(YEAR, get(YEAR) - 1)
        set(MONTH, month + 1)
      }
      set(DAY_OF_MONTH, 1)
      set(HOUR_OF_DAY, 0)
      set(MINUTE, 0)
      set(SECOND, 0)
      set(MILLISECOND, 0)
    }
    .timeInMillis
  private val todayFormat = SimpleDateFormat("HH:mm", Locale.US)
  private val pastYearFormat = SimpleDateFormat("MMM d HH:mm", Locale.US)
  private val olderThanAYearFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US)
  private val todayFormat12Hour = SimpleDateFormat("h:mm a", Locale.US)
  private val pastYearFormat12Hour = SimpleDateFormat("MMM d h:mm a", Locale.US)
  private val olderThanAYearFormat12Hour = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)

  init {
    todayFormat.timeZone = timeZone
    pastYearFormat.timeZone = timeZone
    olderThanAYearFormat.timeZone = timeZone
    todayFormat12Hour.timeZone = timeZone
    pastYearFormat12Hour.timeZone = timeZone
    olderThanAYearFormat12Hour.timeZone = timeZone
  }

  /**
   * Format the date.
   */
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
