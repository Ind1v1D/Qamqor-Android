package com.example.qamqorapp.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.components.QamqorField
import org.koin.androidx.compose.koinViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

@Composable
fun SetupRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<SetupViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) {
        if (state.done) {
            viewModel.consumeDone()
            onDone()
        }
    }

    SetupScreen(
        uiState = state,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}

@Composable
fun SetupScreen(
    uiState: SetupUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current

    Box(modifier = modifier.fillMaxSize().background(colors.panel)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.SetupAvatar.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.pine),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🐾", style = MaterialTheme.typography.displaySmall)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.displaySmall,
                color = colors.pine,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
            )

            Spacer(modifier = Modifier.height(28.dp))

            SetupField(
                label = stringResource(R.string.login_username),
                value = uiState.username,
                onValueChange = onUsernameChange,
                placeholder = stringResource(R.string.login_username_hint),
                imeAction = ImeAction.Next,
                isError = uiState.usernameError,
                errorText = stringResource(R.string.error_required),
                leading = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = colors.inkSoft,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )

            Spacer(modifier = Modifier.height(Dimens.FieldGap.dp))

            SetupField(
                label = stringResource(R.string.login_password),
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.login_password_hint),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.passwordError,
                errorText = stringResource(R.string.error_required),
                leading = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = colors.inkSoft,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.login_demo_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colors.amber,
                modifier = Modifier.clickable {
                    onUsernameChange("emilys")
                    onPasswordChange("emilyspass")
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.error) {
                Text(
                    text = stringResource(R.string.login_error),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.lostInk,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SubmitButton.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.amber)
                    .clickable(enabled = !uiState.isSaving) { onSubmit() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.setup_submit),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SetupField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    errorText: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    QamqorField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        keyboardType = keyboardType,
        imeAction = imeAction,
        visualTransformation = visualTransformation,
        isError = isError,
        errorText = errorText,
        leading = leading,
    )
}
