package com.example.qamqorapp.ui.setup

import androidx.annotation.StringRes
import com.example.qamqorapp.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

enum class AuthError(@param:StringRes val messageRes: Int) {
    WEAK_PASSWORD(R.string.auth_error_weak_password),
    INVALID_CREDENTIALS(R.string.auth_error_invalid_credentials),
    EMAIL_IN_USE(R.string.auth_error_email_in_use),
    NETWORK(R.string.auth_error_network),
    GENERIC(R.string.auth_error_generic),
}

fun Throwable.toAuthError(): AuthError = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
    is FirebaseNetworkException -> AuthError.NETWORK
    else -> AuthError.GENERIC
}
