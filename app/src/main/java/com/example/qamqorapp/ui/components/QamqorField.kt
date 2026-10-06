package com.example.qamqorapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * The form field of the design concept: **44 dp tall, 10 dp radius, parchment fill
 * with a `line` hairline, `pine` on focus or error**, plus an 11sp label above it —
 * exactly `.field label { font-size:11px }` over `.field .input { border-radius:10px;
 * padding:10px 12px; background:var(--bg) }`.
 *
 * This is built on [BasicTextField] rather than Material3's `TextField` on purpose:
 * M3's decoration box carries its own minimum height, so squeezing it into the
 * spec's 44 dp made it lay its text out taller than the wrapper and the `.clip()`
 * on this Box then sliced the glyphs off at the baseline. [BasicTextField] has no
 * opinions of its own — the wrapper decides the height, and [decorationBox] decides
 * where the placeholder, the leading/trailing slots and the text itself sit.
 *
 * @param label optional 11sp caption rendered above the box.
 * @param textTint forces the text colour (used by the dropdown so a placeholder can
 *   be ink-soft while a chosen value is full-strength ink).
 * @param onBoxClick when set, a transparent overlay absorbs every tap so the whole
 *   box behaves like a button — that is how the district menu opens without the
 *   field grabbing keyboard focus.
 */
@Composable
fun QamqorField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minHeight: Dp = Dimens.InputHeight.dp,
    isError: Boolean = false,
    errorText: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    textTint: Color? = null,
    containerColor: Color? = null,
    onBoxClick: (() -> Unit)? = null,
) {
    val colors = LocalQamqorColors.current
    val shape = RoundedCornerShape(Dimens.InputRadius.dp)
    val tint = textTint ?: colors.ink
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = tint)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) colors.pine else colors.inkSoft,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .clip(shape)
                .background(containerColor ?: colors.bg)
                .border(
                    width = 1.dp,
                    color = if (isError) colors.pine else colors.line,
                    shape = shape,
                ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                readOnly = readOnly,
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.pine),
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                decorationBox = { innerTextField ->
                    // `.field .input` is 12px on the sides, 10px top and bottom.
                    // The single-line case centres that box in the 44 dp; the
                    // description field (96 dp) pins the text to the top instead.
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 12.dp,
                                vertical = if (singleLine) 0.dp else 10.dp,
                            ),
                        verticalAlignment = if (singleLine) {
                            Alignment.CenterVertically
                        } else {
                            Alignment.Top
                        },
                    ) {
                        if (leading != null) {
                            leading()
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty() && placeholder.isNotEmpty()) {
                                Text(text = placeholder, style = textStyle.copy(color = colors.inkSoft))
                            }
                            innerTextField()
                        }

                        if (trailing != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            trailing()
                        }
                    }
                },
            )

            // A transparent overlay absorbs the taps for dropdown-style fields: a
            // `readOnly` field would otherwise swallow them trying to focus.
            if (onBoxClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onBoxClick),
                )
            }
        }

        if (isError && errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = colors.pine,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
