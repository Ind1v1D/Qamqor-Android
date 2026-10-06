package com.example.qamqorapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.qamqorapp.R
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostStatus

/**
 * Single place where a status becomes user-visible text and colour, so the segment
 * labels, the tag on the card, the info cell on the detail screen and the tab
 * filter can never drift apart.
 */
@Composable
fun statusLabel(status: PostStatus): String = stringResource(
    when (status) {
        PostStatus.LOST -> R.string.status_lost
        PostStatus.FOUND -> R.string.status_found
        PostStatus.ADOPT -> R.string.status_adopt
    }
)

/** Same mapping, without a Composable — for code that only needs the resource id. */
@StringRes
fun statusStringRes(status: PostStatus): Int = when (status) {
    PostStatus.LOST -> R.string.status_lost
    PostStatus.FOUND -> R.string.status_found
    PostStatus.ADOPT -> R.string.status_adopt
}
