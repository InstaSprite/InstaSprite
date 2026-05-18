package com.instasprite.app.utils

import android.content.Context
import com.instasprite.app.R
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun formatTimeAgo(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> context.getString(R.string.time_just_now)
            diff < 3_600_000 -> context.getString(R.string.time_minutes_ago, diff / 60_000)
            diff < 86_400_000 -> context.getString(R.string.time_hours_ago, diff / 3_600_000)
            diff < 604_800_000 -> context.getString(R.string.time_days_ago, diff / 86_400_000)
            else -> {
                val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }

    fun formatTimeAgo(context: Context, dateTime: LocalDateTime): String {
        val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return formatTimeAgo(context, timestamp)
    }
}


