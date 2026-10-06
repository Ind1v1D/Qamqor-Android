package com.example.qamqorapp.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.components.QamqorField
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * The two dialogs the design implies but does not draw: editing the local profile
 * and confirming a destructive action («Выйти», «Удалить»).
 *
 * They share the app's surfaces and type so they read as part of the same system
 * rather than as stock Material dialogs dropped on top.
 */
@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalQamqorColors.current
    val cancelLabel = stringResource(R.string.cancel)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(colors.panel)
                .padding(20.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DialogButton(
                    label = cancelLabel,
                    primary = false,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                DialogButton(
                    label = confirmLabel,
                    primary = true,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (primary) colors.pine else colors.bg)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (primary) Color.White else colors.ink,
        )
    }
}

/**
 * Name + phone editor for the local profile (there is no backend, so "editing the
 * account" is just rewriting the single `profile` row).
 */
@Composable
fun EditProfileDialog(
    initialName: String,
    initialPhone: String,
    onSave: (name: String, phone: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalQamqorColors.current
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }

    val saveLabel = stringResource(R.string.save)
    val cancelLabel = stringResource(R.string.cancel)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(colors.panel)
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_edit),
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
            Spacer(modifier = Modifier.height(16.dp))

            DialogField(
                label = stringResource(R.string.setup_name),
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.setup_name_hint),
            )
            Spacer(modifier = Modifier.height(12.dp))
            DialogField(
                label = stringResource(R.string.setup_phone),
                value = phone,
                onValueChange = { phone = it },
                placeholder = stringResource(R.string.setup_phone_hint),
                keyboardType = KeyboardType.Phone,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DialogButton(
                    label = cancelLabel,
                    primary = false,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                DialogButton(
                    label = saveLabel,
                    primary = true,
                    onClick = {
                        if (name.isNotBlank()) onSave(name.trim(), phone.trim())
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = LocalQamqorColors.current
    QamqorField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        keyboardType = keyboardType,
        // The dialog sits on `panel`, so the input has to be the parchment tone.
        containerColor = colors.bg,
    )
}
