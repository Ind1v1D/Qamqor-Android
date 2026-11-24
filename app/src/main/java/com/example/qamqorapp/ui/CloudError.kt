package com.example.qamqorapp.ui

import androidx.annotation.StringRes
import com.example.qamqorapp.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

enum class CloudError(@param:StringRes val messageRes: Int) {
    PERMISSION(R.string.cloud_error_permission),
    OFFLINE(R.string.cloud_error_offline),
    NOT_SIGNED_IN(R.string.cloud_error_signed_out),
    GENERIC(R.string.cloud_error_generic),
}

fun Throwable.toCloudError(): CloudError = when (this) {
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> CloudError.PERMISSION
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> CloudError.NOT_SIGNED_IN
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> CloudError.OFFLINE
        else -> CloudError.GENERIC
    }
    is FirebaseNetworkException -> CloudError.OFFLINE
    is IllegalStateException -> CloudError.NOT_SIGNED_IN
    else -> CloudError.GENERIC
}
