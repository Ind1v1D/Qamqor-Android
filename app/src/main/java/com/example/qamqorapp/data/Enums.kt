package com.example.qamqorapp.data

import androidx.annotation.StringRes
import com.example.qamqorapp.R

enum class PostStatus {
    LOST,
    FOUND,
    ADOPT,
}

enum class Species {
    DOG,
    CAT,
    OTHER,
}

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
