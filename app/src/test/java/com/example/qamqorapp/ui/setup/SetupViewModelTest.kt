package com.example.qamqorapp.ui.setup

import com.example.qamqorapp.data.AuthClient
import com.example.qamqorapp.ui.feed.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `successful login marks done`() = runTest {
        val viewModel = SetupViewModel(FakeAuthClient())
        viewModel.onUsernameChange("emilys")
        viewModel.onPasswordChange("emilyspass")

        viewModel.submit()

        assertTrue(viewModel.uiState.value.done)
        assertFalse(viewModel.uiState.value.isSaving)
        assertFalse(viewModel.uiState.value.error)
    }

    @Test
    fun `failed login surfaces error`() = runTest {
        val viewModel = SetupViewModel(FakeAuthClient(fail = true))
        viewModel.onUsernameChange("emilys")
        viewModel.onPasswordChange("wrong")

        viewModel.submit()

        assertTrue(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.done)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `blank fields are flagged`() = runTest {
        val viewModel = SetupViewModel(FakeAuthClient())

        viewModel.submit()

        assertTrue(viewModel.uiState.value.usernameError)
        assertTrue(viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isSaving)
    }
}

private class FakeAuthClient(
    private val fail: Boolean = false,
) : AuthClient {

    override var isLoggedIn = false

    override suspend fun login(username: String, password: String) {
        if (fail) error("login failed")
        isLoggedIn = true
    }

    override suspend fun logout() {
        isLoggedIn = false
    }
}
