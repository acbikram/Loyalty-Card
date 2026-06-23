package com.universalwallet.loyalty.core.locale

import android.text.TextUtils
import android.view.View
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

/**
 * Locale-aware formatting helpers. Always format dates/numbers through these
 * rather than hard-coding patterns, so Arabic (and future locales) render with
 * the correct digits, separators, and direction.
 */
object LocalizedFormat {

    fun date(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(epochMillis))

    fun dateTime(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(epochMillis))

    fun number(value: Number, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getNumberInstance(locale).format(value)

    fun percent(fraction: Double, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getPercentInstance(locale).format(fraction)

    /** True when the locale lays out right-to-left (e.g. Arabic). */
    fun isRtl(locale: Locale = Locale.getDefault()): Boolean =
        TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
}
