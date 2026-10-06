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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.components.QamqorField
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * The first-run screen — this app's stand-in for a login.
 *
 * An empty `profile` table is what puts the shell here instead of on the feed, so
 * there is no separate "logged out" state to model: the screen simply writes the
 * row and reports `done`, and the shell swaps the start destination.
 */
@Composable
fun SetupScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<SetupViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalQamqorColors.current

    LaunchedEffect(state.done) {
        if (state.done) {
            viewModel.consumeDone()
            onDone()
        }
    }

    // `.phone` in the concept is `--panel` — the default surface that every
    // screen without its own background inherits. Setup follows that rule so the
    // parchment inputs sit *on* something.
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
            // Brand block: the warm gradient tile instead of a logo asset.
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
                label = stringResource(R.string.setup_name),
                value = state.name,
                onValueChange = viewModel::onNameChange,
                placeholder = stringResource(R.string.setup_name_hint),
                isError = state.nameError,
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
                label = stringResource(R.string.setup_phone),
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                placeholder = stringResource(R.string.setup_phone_hint),
                keyboardType = KeyboardType.Phone,
                isError = state.phoneError,
                errorText = stringResource(R.string.error_phone),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary call to action — amber, the single bright accent.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SubmitButton.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.amber)
                    .clickable(enabled = !state.isSaving) { viewModel.submit() },
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
        isError = isError,
        errorText = errorText,
        leading = leading,
    )
}
