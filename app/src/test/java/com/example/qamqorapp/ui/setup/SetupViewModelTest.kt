package com.example.qamqorapp.ui.setup

import com.example.qamqorapp.data.AuthClient
import com.example.qamqorapp.data.PushTokenRepository
import com.example.qamqorapp.ui.feed.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `successful login marks done and syncs push token`() = runTest {
        val tokens = FakePushTokens()
        val viewModel = SetupViewModel(FakeAuthClient(), tokens)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("secret1")

        viewModel.submit()

        assertTrue(viewModel.uiState.value.done)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.error)
        assertEquals(1, tokens.syncCalls)
    }

    @Test
    fun `failed login surfaces typed error`() = runTest {
        val tokens = FakePushTokens()
        val viewModel = SetupViewModel(FakeAuthClient(failure = IllegalStateException("boom")), tokens)
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("wrong")

        viewModel.submit()

        assertEquals(AuthError.GENERIC, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.done)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(0, tokens.syncCalls)
    }

    @Test
    fun `editing a field clears the error`() = runTest {
        val viewModel = SetupViewModel(FakeAuthClient(failure = IllegalStateException("boom")), FakePushTokens())
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("wrong")
        viewModel.submit()

        viewModel.onPasswordChange("other1")

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `register calls auth and marks done`() = runTest {
        val auth = FakeAuthClient()
        val viewModel = SetupViewModel(auth, FakePushTokens())
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("secret1")

        viewModel.register()

        assertTrue(auth.registered)
        assertTrue(viewModel.uiState.value.done)
    }

    @Test
    fun `blank fields are flagged`() = runTest {
        val viewModel = SetupViewModel(FakeAuthClient(), FakePushTokens())

        viewModel.submit()

        assertTrue(viewModel.uiState.value.emailError)
        assertTrue(viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `unknown exceptions map to generic error`() {
        assertEquals(AuthError.GENERIC, RuntimeException("raw message").toAuthError())
    }
}

private class FakePushTokens : PushTokenRepository {
    var syncCalls = 0

    override fun syncToken() {
        syncCalls++
    }

    override fun saveToken(token: String) = Unit

    override suspend fun clearToken() = Unit
}

private class FakeAuthClient(
    private val failure: Exception? = null,
) : AuthClient {

    override var isLoggedIn = false
    var registered = false

    override suspend fun login(email: String, password: String) {
        failure?.let { throw it }
        isLoggedIn = true
    }

    override suspend fun register(email: String, password: String) {
        failure?.let { throw it }
        registered = true
        isLoggedIn = true
    }

    override suspend fun logout() {
        isLoggedIn = false
    }
}
