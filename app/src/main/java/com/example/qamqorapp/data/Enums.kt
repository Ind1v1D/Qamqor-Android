package com.example.qamqorapp.data

import androidx.annotation.StringRes
import com.example.qamqorapp.R

/**
 * The three statuses the design concept switches between in the segments row.
 *
 * Stored as plain strings in Room (see Converters) so that renaming an enum
 * constant later does not invalidate rows — SQLite only ever sees "LOST".
 */
enum class PostStatus {
    LOST,
    FOUND,
    ADOPT,
}

/** Species filter — the «Собаки» / «Кошки» chips of the feed. */
enum class Species {
    DOG,
    CAT,
    OTHER,
}

/**
 * Almaty districts used as the location filter.
 *
 * @property labelRes the district's name in the current locale («Алмалинский р-н»
 *   in Russian, «Almalinsky district» in English) — the *data* only stores the enum
 *   name, the *text* always comes from resources so it is translatable.
 */
enum class District(@param:StringRes val labelRes: Int) {
    ALMALINSKY(R.string.district_almalinsky),
    MEDEUSKY(R.string.district_medeusky),
    BOSTANDYK(R.string.district_bostandyk),
    NAURYZBAY(R.string.district_nauryzbay),
    TURKSIB(R.string.district_turksib),
    ALATAU(R.string.district_alatau),
    YESSIL(R.string.district_yessil),
    ZARECHNY(R.string.district_zarechny),
}
