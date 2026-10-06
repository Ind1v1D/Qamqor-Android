package com.example.qamqorapp.util

import android.content.Context
import com.example.qamqorapp.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Turns an epoch-millis timestamp into the phrases the mockups use:
 * «вчера», «2 дня назад», «сегодня».
 *
 * Kept out of the composables so the same logic can be unit-tested and reused by
 * both the feed card («Алмалинский р-н · вчера») and the detail header
 * («видели вчера, 18:40»).
 *
 * Order matters: the *calendar* checks (today / yesterday) win over the raw
 * elapsed-time checks, otherwise a post made at 23:00 would read "20 hours ago"
 * at 21:00 the next day instead of «вчера».
 */
object RelativeTime {

    fun format(context: Context, timestamp: Long, now: Long = System.currentTimeMillis()): String {
        if (timestamp <= 0) return ""

        val elapsed = now - timestamp
        if (elapsed < TimeUnit.MINUTES.toMillis(5)) {
            return context.getString(R.string.time_just_now)
        }
        // Sub-hour posts keep their exact age even across midnight: "30 min ago"
        // reads better than "yesterday" at 00:15.
        if (elapsed < TimeUnit.MINUTES.toMillis(60)) {
            return context.getString(
                R.string.time_minutes,
                TimeUnit.MILLISECONDS.toMinutes(elapsed).toInt(),
            )
        }

        val then = calendarOf(timestamp)
        val today = calendarOf(now)

        val sameDay = then.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            then.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        if (sameDay) return context.getString(R.string.time_today)

        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = then.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            then.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) return context.getString(R.string.time_yesterday)

        return when {
            elapsed < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(elapsed).toInt().coerceAtLeast(1)
                context.resources.getQuantityString(R.plurals.days_count, days, days)
            }

            elapsed < TimeUnit.DAYS.toMillis(28) -> {
                val weeks = (TimeUnit.MILLISECONDS.toDays(elapsed).toInt() / 7).coerceAtLeast(1)
                context.resources.getQuantityString(R.plurals.weeks_count, weeks, weeks)
            }

            else -> {
                val months = (TimeUnit.MILLISECONDS.toDays(elapsed).toInt() / 30)
                    .coerceAtLeast(1)
                context.resources.getQuantityString(R.plurals.months_count, months, months)
            }
        }
    }

    /**
     * «вчера, 18:40» — a relative day plus the wall-clock time, matching the detail
     * screen's `detail_seen` pattern.
     */
    fun formatWithClock(
        context: Context,
        timestamp: Long,
        now: Long = System.currentTimeMillis(),
    ): String {
        if (timestamp <= 0) return ""
        val day = format(context, timestamp, now)
        val clock = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        return String.format(
            context.resources.configuration.locales[0],
            "%s, %02d:%02d",
            day,
            clock.get(Calendar.HOUR_OF_DAY),
            clock.get(Calendar.MINUTE),
        )
    }

    private fun calendarOf(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }
}
