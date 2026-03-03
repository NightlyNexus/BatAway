package com.nightlynexus.bataway

import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DECEMBER
import java.util.Calendar.FEBRUARY
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.JANUARY
import java.util.Calendar.MARCH
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.YEAR
import java.util.Locale
import java.util.TimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DateFormatterTest {
  @Test fun today() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 20)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, true, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 20)
            set(HOUR_OF_DAY, 13)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("13:59")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 20)
            set(HOUR_OF_DAY, 9)
            set(MINUTE, 2)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("09:02")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 20)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("00:00")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 19)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Feb 19 23:59")
  }

  @Test fun today12Hour() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 20)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, false, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 20)
            set(HOUR_OF_DAY, 13)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("1:59 PM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 20)
            set(HOUR_OF_DAY, 9)
            set(MINUTE, 2)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("9:02 AM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 20)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("12:00 AM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 19)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Feb 19 11:59 PM")
  }

  @Test fun pastYear() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 20)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, true, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, MARCH)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("Mar 1 00:00")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 28)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Feb 28, 2019 23:59")
  }

  @Test fun pastYear12Hour() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 20)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, false, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, MARCH)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("Mar 1 12:00 AM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 28)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Feb 28, 2019 11:59 PM")
  }

  @Test fun older() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 20)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, true, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 28)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Feb 28, 2019 23:59")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, JANUARY)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 2)
            set(MINUTE, 4)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("Jan 1, 2019 02:04")
  }

  @Test fun older12Hour() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 20)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, false, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 28)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Feb 28, 2019 11:59 PM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, JANUARY)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 2)
            set(MINUTE, 4)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("Jan 1, 2019 2:04 AM")
  }

  @Test fun december() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, DECEMBER)
        set(DAY_OF_MONTH, 25)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, true, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, DECEMBER)
            set(DAY_OF_MONTH, 25)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("00:00")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, DECEMBER)
            set(DAY_OF_MONTH, 24)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Dec 24 23:59")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, JANUARY)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("Jan 1 00:00")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, DECEMBER)
            set(DAY_OF_MONTH, 31)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Dec 31, 2019 23:59")
  }

  @Test fun december12Hour() {
    val timeZone = TimeZone.getDefault()
    val locale = Locale.getDefault()
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2020)
        set(MONTH, DECEMBER)
        set(DAY_OF_MONTH, 25)
        set(HOUR_OF_DAY, 14)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, false, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, DECEMBER)
            set(DAY_OF_MONTH, 25)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("12:00 AM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, DECEMBER)
            set(DAY_OF_MONTH, 24)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Dec 24 11:59 PM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2020)
            set(MONTH, JANUARY)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("Jan 1 12:00 AM")
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2019)
            set(MONTH, DECEMBER)
            set(DAY_OF_MONTH, 31)
            set(HOUR_OF_DAY, 23)
            set(MINUTE, 59)
            set(SECOND, 59)
            set(MILLISECOND, 999)
          }.time
      )
    ).isEqualTo("Dec 31, 2019 11:59 PM")
  }

  @Test fun tokyoTimezone() {
    val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
    val locale = Locale.JAPAN
    val now = Calendar.getInstance(timeZone, locale)
      .apply {
        set(YEAR, 2026)
        set(MONTH, FEBRUARY)
        set(DAY_OF_MONTH, 1)
        set(HOUR_OF_DAY, 12)
        set(MINUTE, 0)
        set(SECOND, 0)
        set(MILLISECOND, 0)
      }
    val dateFormatter = DateFormatter(timeZone, locale, true, now.time)
    assertThat(
      dateFormatter.format(
        Calendar.getInstance(timeZone, locale)
          .apply {
            set(YEAR, 2026)
            set(MONTH, FEBRUARY)
            set(DAY_OF_MONTH, 1)
            set(HOUR_OF_DAY, 1)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
          }.time
      )
    ).isEqualTo("01:00")
  }
}
