package com.example.qamqorapp.data

import android.content.Context
import androidx.annotation.StringRes
import com.example.qamqorapp.R
import java.util.Calendar

/**
 * The demo content from the design concept, so the app is readable the first time
 * it is opened instead of showing an empty list.
 *
 * Every word of it is read from string resources rather than hardcoded here:
 * these posts are *shown* to the user, so an English install must get English
 * posts instead of the Russian originals from the mockup. (The enum columns —
 * status, species, district — already resolve through their own `labelRes`.)
 *
 * Timestamps are *offsets from now* rather than fixed dates: that way the feed
 * always says «вчера» / «2 дня назад» exactly like the mockups, whether the app is
 * first launched today or in six months. The locale is sampled once, at seed time;
 * changing the system language later does not rewrite rows that are already in
 * Room — same rule every other seeded field follows.
 *
 * (java.time is deliberately avoided — minSdk 24 would require core library
 * desugaring for it; Calendar is always available.)
 */
object Seed {

    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    /** `now` minus a number of days, at 18:40 local time — «видели вчера, 18:40». */
    private fun atLocalTime(now: Long, daysAgo: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Shorthand so each post below reads as a list of fields, not of `getString` calls. */
    private fun Context.text(@StringRes id: Int): String = getString(id)

    fun posts(context: Context, now: Long = System.currentTimeMillis()): List<Post> = listOf(
        // 1 — «Потерян» — the hero card of the feed and the detail mockup.
        Post(
            status = PostStatus.LOST,
            species = Species.DOG,
            name = context.text(R.string.seed_p1_name),
            breed = context.text(R.string.seed_p1_breed),
            district = District.ALMALINSKY,
            address = context.text(R.string.seed_p1_address),
            marks = context.text(R.string.seed_p1_marks),
            description = context.text(R.string.seed_p1_description),
            emoji = "🐕",
            phone = "+7 707 000 00 00",
            author = context.text(R.string.seed_author_me),
            createdAt = atLocalTime(now, daysAgo = 1, hour = 19, minute = 5),
            lastSeenAt = atLocalTime(now, daysAgo = 1, hour = 18, minute = 40),
            isFavorite = true,
            isMine = true,
        ),

        // 2 — «Найдена» — no collar, found 2 days ago.
        Post(
            status = PostStatus.FOUND,
            species = Species.CAT,
            name = context.text(R.string.seed_p2_name),
            breed = context.text(R.string.seed_p2_breed),
            district = District.MEDEUSKY,
            address = context.text(R.string.seed_p2_address),
            marks = context.text(R.string.seed_p2_marks),
            description = context.text(R.string.seed_p2_description),
            emoji = "🐈",
            phone = "+7 700 111 22 33",
            author = context.text(R.string.seed_author_dauren),
            createdAt = atLocalTime(now, daysAgo = 2, hour = 11, minute = 20),
            lastSeenAt = atLocalTime(now, daysAgo = 2, hour = 11, minute = 20),
            isFavorite = true,
        ),

        // 3 — «Пристройство» — from the приют «Дос».
        Post(
            status = PostStatus.ADOPT,
            species = Species.CAT,
            name = context.text(R.string.seed_p3_name),
            breed = context.text(R.string.seed_p3_breed),
            district = District.BOSTANDYK,
            address = context.text(R.string.seed_p3_address),
            marks = context.text(R.string.seed_p3_marks),
            description = context.text(R.string.seed_p3_description),
            emoji = "🐾",
            phone = "+7 707 000 00 00",
            author = context.text(R.string.seed_author_me),
            createdAt = atLocalTime(now, daysAgo = 5, hour = 10, minute = 0),
            isFavorite = true,
            isMine = true,
        ),

        // 4 — «Потерян» — today.
        Post(
            status = PostStatus.LOST,
            species = Species.DOG,
            name = context.text(R.string.seed_p4_name),
            breed = context.text(R.string.seed_p4_breed),
            district = District.NAURYZBAY,
            address = context.text(R.string.seed_p4_address),
            marks = context.text(R.string.seed_p4_marks),
            description = context.text(R.string.seed_p4_description),
            emoji = "🐕",
            phone = "+7 705 444 55 66",
            author = context.text(R.string.seed_author_me),
            createdAt = now - 3 * HOUR,
            lastSeenAt = now - 3 * HOUR,
            isMine = true,
        ),

        // 5 — «Пристройство» — a dog, so the species chips have both values to filter.
        Post(
            status = PostStatus.ADOPT,
            species = Species.DOG,
            name = context.text(R.string.seed_p5_name),
            breed = context.text(R.string.seed_p5_breed),
            district = District.ALATAU,
            address = context.text(R.string.seed_p5_address),
            marks = context.text(R.string.seed_p5_marks),
            description = context.text(R.string.seed_p5_description),
            emoji = "🐾",
            phone = "+7 700 111 22 33",
            author = context.text(R.string.seed_author_me),
            createdAt = atLocalTime(now, daysAgo = 8, hour = 14, minute = 30),
            isMine = true,
        ),

        // 6 — a RESOLVED post: visible on the profile (dimmed), gone from the feed.
        Post(
            status = PostStatus.FOUND,
            species = Species.DOG,
            name = context.text(R.string.seed_p6_name),
            breed = context.text(R.string.seed_p6_breed),
            district = District.ZARECHNY,
            address = context.text(R.string.seed_p6_address),
            marks = context.text(R.string.seed_p6_marks),
            description = context.text(R.string.seed_p6_description),
            emoji = "🐕",
            phone = "+7 705 444 55 66",
            author = context.text(R.string.seed_author_me),
            createdAt = atLocalTime(now, daysAgo = 7, hour = 16, minute = 10),
            isResolved = true,
            isMine = true,
        ),

        // 7 — a bird, so Species.OTHER is represented and the filter is honest.
        Post(
            status = PostStatus.FOUND,
            species = Species.OTHER,
            name = context.text(R.string.seed_p7_name),
            breed = context.text(R.string.seed_p7_breed),
            district = District.TURKSIB,
            address = context.text(R.string.seed_p7_address),
            marks = context.text(R.string.seed_p7_marks),
            description = context.text(R.string.seed_p7_description),
            emoji = "🐦",
            phone = "+7 701 222 33 44",
            author = context.text(R.string.seed_author_madina),
            createdAt = now - 26 * HOUR,
            lastSeenAt = now - 26 * HOUR,
        ),
    )
}
