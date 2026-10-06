package com.example.qamqorapp.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import com.example.qamqorapp.ui.components.BackButton
import com.example.qamqorapp.ui.components.QamqorField
import com.example.qamqorapp.ui.components.QamqorTopBar
import com.example.qamqorapp.ui.components.dashedBorder
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * «Новое объявление».
 *
 * The concept's dashed «Добавить фото» box is replaced by an emoji picker — the
 * scope of this build has no photo pipeline — and the district field is a real
 * dropdown over the [District] enum rather than free text, which is exactly what
 * makes the feed's district chips possible.
 *
 * Chrome: 56 dp app bar with a back arrow, scrollable form, and the 48 dp amber
 * «Опубликовать» button pinned to the bottom (above the nav bar and the keyboard).
 */
@Composable
fun CreateScreen(
    onBack: () -> Unit,
    onPublished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<CreateViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalQamqorColors.current

    // Prefill the contact phone from the profile the first time we compose.
    LaunchedEffect(Unit) { viewModel.prefillFromProfile() }

    // The row is in Room: hand control back to the shell, which pops this screen.
    LaunchedEffect(state.published) {
        if (state.published) {
            viewModel.consumePublished()
            onPublished()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.panel)) {
        Column(modifier = Modifier.fillMaxSize()) {

            QamqorTopBar(
                title = stringResource(R.string.create_title),
                navigationIcon = {
                    BackButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.detail_back),
                    )
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    // `.form{padding:16px}` — 16 all round, 12px between rows.
                    .padding(Dimens.ContentPadding.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.FieldGap.dp),
            ) {
                // ---- category: 3 chips, 40 dp, equal width -------------------
                // No caption in the concept: `.catrow` sits at the top of `.form`.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CategoryChip(
                        label = stringResource(R.string.seg_lost),
                        selected = state.status == PostStatus.LOST,
                        onClick = { viewModel.selectStatus(PostStatus.LOST) },
                        modifier = Modifier.weight(1f),
                    )
                    CategoryChip(
                        label = stringResource(R.string.seg_found),
                        selected = state.status == PostStatus.FOUND,
                        onClick = { viewModel.selectStatus(PostStatus.FOUND) },
                        modifier = Modifier.weight(1f),
                    )
                    CategoryChip(
                        label = stringResource(R.string.seg_adopt),
                        selected = state.status == PostStatus.ADOPT,
                        onClick = { viewModel.selectStatus(PostStatus.ADOPT) },
                        modifier = Modifier.weight(1f),
                    )
                }

                // ---- illustration: the concept's photo box, as an emoji picker -
                EmojiPicker(
                    selected = state.emoji,
                    onSelect = viewModel::selectEmoji,
                )

                // ---- fields -------------------------------------------------
                LabeledField(
                    label = stringResource(R.string.create_name),
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = stringResource(R.string.create_name_hint),
                    isError = state.nameError,
                    errorText = stringResource(R.string.error_required),
                )

                DistrictField(
                    selected = state.district,
                    isError = state.districtError,
                    onSelect = viewModel::selectDistrict,
                )

                LabeledField(
                    label = stringResource(R.string.create_phone),
                    value = state.phone,
                    onValueChange = viewModel::onPhoneChange,
                    placeholder = stringResource(R.string.create_phone_hint),
                    keyboardType = KeyboardType.Phone,
                    isError = state.phoneError,
                    errorText = stringResource(R.string.error_phone),
                )

                LabeledField(
                    label = stringResource(R.string.create_description),
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = stringResource(R.string.create_description_hint),
                    minHeight = 96,
                )

                Spacer(modifier = Modifier.height(56.dp))
            }
        }

        // ---- pinned «Опубликовать», 48 dp ----------------------------------
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.panel)
                .navigationBarsPadding()
                .padding(
                    horizontal = Dimens.ContentPadding.dp,
                    vertical = 12.dp,
                ),
        ) {
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
                    text = stringResource(R.string.create_submit),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
    }
}

/** 40 dp category chip; the selected one flips to pine, exactly like the mockup. */
@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    Box(
        modifier = modifier
            .height(Dimens.CategoryChip.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.pine else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.pine else colors.line,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else colors.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The 96 dp dashed box of the concept, holding the emoji choices. */
@Composable
private fun EmojiPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.PhotoBox.dp)
            .clip(RoundedCornerShape(14.dp))
            // `border:1.5px dashed var(--line)` — no fill in the concept, so the
            // panel of the form shows straight through the dashes.
            .dashedBorder(
                color = colors.line,
                width = 1.5.dp,
                radius = 14.dp,
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.create_emoji_hint),
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSoft,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🐕", "🐈", "🐾", "🐦", "🐹", "🐢").forEach { emoji ->
                val isSelected = emoji == selected
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        // Same surface treatment as every other input: parchment
                        // with a hairline, amber-soft when chosen.
                        .background(if (isSelected) colors.amberSoft else colors.bg)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.amber else colors.line,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { onSelect(emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

/** 44 dp input with an 11sp label above it and inline validation. */
@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    minHeight: Int = Dimens.InputHeight,
    isError: Boolean = false,
    errorText: String? = null,
) {
    QamqorField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        keyboardType = keyboardType,
        minHeight = minHeight.dp,
        singleLine = minHeight <= Dimens.InputHeight,
        isError = isError,
        errorText = errorText,
    )
}

/** District dropdown: 44 dp field that opens a Material3 menu of the enum. */
@Composable
private fun DistrictField(
    selected: District?,
    isError: Boolean,
    onSelect: (District) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    // The menu has to live inside the same Box as the field so it can anchor to
    // the field's bounds; QamqorField is what actually draws the box, and it
    // hands every tap on that box to `onBoxClick` instead of trying to focus.
    Box(modifier = modifier.fillMaxWidth()) {
        QamqorField(
            value = selected?.let { stringResource(it.labelRes) }.orEmpty(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.create_district),
            placeholder = stringResource(R.string.create_district_hint),
            readOnly = true,
            isError = isError,
            errorText = stringResource(R.string.error_required),
            textTint = if (selected == null) {
                LocalQamqorColors.current.inkSoft
            } else {
                LocalQamqorColors.current.ink
            },
            trailing = { Text("▾", color = LocalQamqorColors.current.inkSoft, fontSize = 14.sp) },
            onBoxClick = { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            District.entries.forEach { district ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(district.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(district)
                    },
                )
            }
        }
    }
}

