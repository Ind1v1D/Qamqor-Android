package com.example.qamqorapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.qamqorapp.R
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostStatus

@Composable
fun statusLabel(status: PostStatus): String = stringResource(
    when (status) {
        PostStatus.LOST -> R.string.status_lost
        PostStatus.FOUND -> R.string.status_found
        PostStatus.ADOPT -> R.string.status_adopt
    }
)

@StringRes
fun statusStringRes(status: PostStatus): Int = when (status) {
    PostStatus.LOST -> R.string.status_lost
    PostStatus.FOUND -> R.string.status_found
    PostStatus.ADOPT -> R.string.status_adopt
}
