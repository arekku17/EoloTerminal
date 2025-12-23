package com.argento.eoloapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatDetailDate(timestamp: Long, timeZoneId: String? = null): String {
    val sdf = SimpleDateFormat("HH:mm - dd/MM/yy", Locale.getDefault())
    if (timeZoneId != null) {
        sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
    }
    return sdf.format(Date(timestamp))
}
