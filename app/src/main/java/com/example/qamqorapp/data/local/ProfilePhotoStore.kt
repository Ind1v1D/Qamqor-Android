package com.example.qamqorapp.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
class ProfilePhotoStore(
    private val dir: File,
    private val authority: String,
) {
    private val avatar = File(dir, "avatar.jpg")
    private val tmp = File(dir, "avatar_tmp.jpg")
    private val staging = File(dir, "avatar_new.jpg")

    private val _version = MutableStateFlow(if (avatar.exists()) avatar.lastModified() else 0L)
    val version: StateFlow<Long> = _version.asStateFlow()

    fun newCaptureUri(context: Context): Uri {
        dir.mkdirs()
        tmp.delete()
        return FileProvider.getUriForFile(context, authority, tmp)
    }
    fun commitCapture(): Boolean {
        if (!tmp.exists() || tmp.length() == 0L) {
            tmp.delete()
            return false
        }
        val moved = tmp.renameTo(avatar) || runCatching {
            tmp.copyTo(staging, overwrite = true)
            check(staging.renameTo(avatar))
        }.isSuccess
        staging.delete()
        tmp.delete()
        if (moved) _version.value = maxOf(System.currentTimeMillis(), _version.value + 1)
        return moved
    }

    fun discardCapture() {
        tmp.delete()
    }

    fun photoFile(): File? = avatar.takeIf { it.exists() }

    fun delete() {
        tmp.delete()
        avatar.delete()
        _version.value = 0L
    }
}
