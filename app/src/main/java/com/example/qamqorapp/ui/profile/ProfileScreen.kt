package com.example.qamqorapp.ui.profile

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.ThemeMode
import com.example.qamqorapp.ui.components.EmojiThumb
import com.example.qamqorapp.ui.components.ResolvedTag
import com.example.qamqorapp.ui.components.SelectableChip
import com.example.qamqorapp.ui.components.StatusTag
import com.example.qamqorapp.ui.dialogs.ConfirmDialog
import com.example.qamqorapp.ui.dialogs.EditProfileDialog
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.util.RelativeTime
import kotlinx.coroutines.launch

@Composable
fun ProfileRoute(
    onSignOut: () -> Unit,
    onOpenPost: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<ProfileViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val simulateError by viewModel.simulateError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showPermanentlyDenied by rememberSaveable { mutableStateOf(false) }
    var rationaleBefore by rememberSaveable { mutableStateOf(false) }
    var askedBefore by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onPhotoCaptured(success)
    }
    val launchCamera: () -> Unit = {
        scope.launch {
            try {
                takePicture.launch(viewModel.newCaptureUri(context))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, R.string.photo_no_camera, Toast.LENGTH_LONG).show()
            }
        }
    }
    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            val rationaleAfter = context.findActivity()?.let { shouldShowCameraRationale(it) } == true
            if (!rationaleAfter && (rationaleBefore || askedBefore)) {
                showPermanentlyDenied = true
            }
        }
    }
    val askForPermission = {
        rationaleBefore = context.findActivity()?.let { shouldShowCameraRationale(it) } == true
        askedBefore = viewModel.askedCameraBefore()
        viewModel.markCameraAsked()
        requestPermission.launch(Manifest.permission.CAMERA)
    }
    val onTakePhoto = {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(context, R.string.photo_no_camera, Toast.LENGTH_LONG).show()
        } else {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            val activity = context.findActivity()
            when {
                granted -> launchCamera()
                activity != null && shouldShowCameraRationale(activity) -> showRationale = true
                else -> askForPermission()
            }
        }
    }

    ProfileScreen(
        uiState = state,
        themeMode = themeMode,
        language = language,
        simulateError = simulateError,
        onOpenPost = onOpenPost,
        onSignOut = onSignOut,
        onTakePhoto = onTakePhoto,
        onRemovePhoto = viewModel::removePhoto,
        onSaveProfile = viewModel::saveProfile,
        onToggleResolved = viewModel::toggleResolved,
        onDeletePost = viewModel::delete,
        onSetTheme = viewModel::setTheme,
        onSetLanguage = { tag -> viewModel.setLanguage(tag, context) },
        onToggleSimulateError = viewModel::setSimulateError,
        modifier = modifier,
    )

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.photo_rationale_title)) },
            text = { Text(stringResource(R.string.photo_rationale_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    askForPermission()
                }) { Text(stringResource(R.string.photo_allow)) }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text(stringResource(R.string.photo_cancel))
                }
            },
        )
    }

    if (showPermanentlyDenied) {
        AlertDialog(
            onDismissRequest = { showPermanentlyDenied = false },
            title = { Text(stringResource(R.string.photo_denied_title)) },
            text = { Text(stringResource(R.string.photo_denied_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showPermanentlyDenied = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    )
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                    }
                }) { Text(stringResource(R.string.photo_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermanentlyDenied = false }) {
                    Text(stringResource(R.string.photo_cancel))
                }
            },
        )
    }
}

private fun shouldShowCameraRationale(activity: Activity): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    themeMode: ThemeMode,
    language: String,
    simulateError: Boolean,
    onOpenPost: (Long) -> Unit,
    onSignOut: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onToggleResolved: (Post) -> Unit,
    onDeletePost: (Post) -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetLanguage: (String) -> Unit,
    onToggleSimulateError: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current

    var showEdit by remember { mutableStateOf(false) }
    var showSignOut by remember { mutableStateOf(false) }
    val photoDescription = stringResource(R.string.photo_avatar_description)

    Box(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.panel)
                    .padding(top = 20.dp, bottom = 14.dp)
                    .padding(horizontal = Dimens.ContentPadding.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.ProfileAvatar.dp)
                        .clip(CircleShape)
                        .background(colors.amberSoft)
                        .clickable(onClick = onTakePhoto)
                        .semantics { contentDescription = photoDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    val photo = uiState.photo
                    if (photo != null) {
                        Image(
                            bitmap = photo,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = colors.adoptInk,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.profile?.name ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Text(
                    text = uiState.profile?.phone ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSoft,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = stringResource(R.string.profile_edit),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.pine,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showEdit = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Text(
                        text = stringResource(
                            if (uiState.photo != null) R.string.photo_change else R.string.photo_take,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.pine,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onTakePhoto)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    if (uiState.photo != null) {
                        Text(
                            text = stringResource(R.string.photo_remove),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkSoft,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRemovePhoto)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.StatsRow.dp)
                        .padding(top = 10.dp, bottom = 10.dp)
                        .border(1.dp, colors.line),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatCell(
                        value = uiState.published,
                        label = stringResource(R.string.stat_published),
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider()
                    StatCell(
                        value = uiState.resolved,
                        label = stringResource(R.string.stat_solved),
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider()
                    StatCell(
                        value = uiState.favorites,
                        label = stringResource(R.string.stat_favorites),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            SettingsSection(
                themeMode = themeMode,
                onThemeMode = onSetTheme,
                language = language,
                onLanguage = onSetLanguage,
                simulateError = simulateError,
                onSimulateError = onToggleSimulateError,
                modifier = Modifier.padding(
                    horizontal = Dimens.ContentPadding.dp,
                    vertical = 8.dp,
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SectionTitle.dp)
                    .padding(start = Dimens.ContentPadding.dp, top = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stringResource(R.string.profile_my_posts),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSoft,
                )
            }

            if (uiState.myPosts.isEmpty() && !uiState.isLoading) {
                Text(
                    text = stringResource(R.string.profile_no_posts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSoft,
                    modifier = Modifier.padding(
                        horizontal = Dimens.ContentPadding.dp,
                        vertical = 16.dp,
                    ),
                )
            } else {
                Column(
                    modifier = Modifier.padding(
                        start = Dimens.ContentPadding.dp,
                        end = Dimens.ContentPadding.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.myPosts.forEach { post ->
                        MyPostRow(
                            post = post,
                            onClick = { onOpenPost(post.id) },
                            onToggleResolved = { onToggleResolved(post) },
                            onDelete = { onDeletePost(post) },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SignOutButton.dp)
                    .padding(
                        start = Dimens.ContentPadding.dp,
                        end = Dimens.ContentPadding.dp,
                        top = 16.dp,
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.panel)
                    .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                    .clickable { showSignOut = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.profile_signout),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.inkSoft,
                )
            }

            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .navigationBarsPadding(),
            )
        }
    }

    if (showEdit) {
        EditProfileDialog(
            initialName = uiState.profile?.name.orEmpty(),
            initialPhone = uiState.profile?.phone.orEmpty(),
            onDismiss = { showEdit = false },
            onSave = { name, phone ->
                onSaveProfile(name, phone)
                showEdit = false
            },
        )
    }

    if (showSignOut) {
        ConfirmDialog(
            title = stringResource(R.string.profile_signout_title),
            body = stringResource(R.string.profile_signout_confirm),
            confirmLabel = stringResource(R.string.profile_signout),
            onDismiss = { showSignOut = false },
            onConfirm = {
                showSignOut = false
                onSignOut()
            },
        )
    }
}

@Composable
private fun StatCell(value: Int, label: String, modifier: Modifier = Modifier) {
    val colors = LocalQamqorColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = colors.ink,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VerticalDivider() {
    val colors = LocalQamqorColors.current
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(colors.line),
    )
}

@Composable
private fun MyPostRow(
    post: Post,
    onClick: () -> Unit,
    onToggleResolved: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalQamqorColors.current
    val context = LocalContext.current

    val alpha = if (post.isResolved) 0.55f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.MyPostRow.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.panel)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.alpha(alpha)) {
            EmojiThumb(post, size = 40)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(alpha),
        ) {
            if (post.isResolved) ResolvedTag() else StatusTag(post.status)
            Text(
                text = post.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(context.getString(post.district.labelRes))
                    val time = RelativeTime.format(context, post.createdAt)
                    if (time.isNotEmpty()) append(" · ").append(time)
                },
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!post.isResolved) {
            RowActionButton(
                icon = Icons.Filled.Check,
                tint = colors.pine,
                onClick = onToggleResolved,
                contentDescription = stringResource(R.string.profile_mark_solved),
            )
        }
        RowActionButton(
            icon = Icons.Filled.Delete,
            tint = colors.inkSoft,
            onClick = onDelete,
            contentDescription = stringResource(R.string.profile_delete),
        )
    }
}

@Composable
private fun RowActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    contentDescription: String,
) {
    val colors = LocalQamqorColors.current
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.bg)
            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
    }
}

@Composable
fun SettingsSection(
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    language: String,
    onLanguage: (String) -> Unit,
    simulateError: Boolean,
    onSimulateError: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.panel)
            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
            .padding(Dimens.ContentPadding.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSoft,
        )
        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(
            title = stringResource(R.string.settings_appearance),
        ) {
            ThemeChip(
                label = stringResource(R.string.theme_system),
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { onThemeMode(ThemeMode.SYSTEM) },
            )
            ThemeChip(
                label = stringResource(R.string.theme_light),
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { onThemeMode(ThemeMode.LIGHT) },
            )
            ThemeChip(
                label = stringResource(R.string.theme_dark),
                selected = themeMode == ThemeMode.DARK,
                onClick = { onThemeMode(ThemeMode.DARK) },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(
            title = stringResource(R.string.settings_language),
        ) {
            ThemeChip(
                label = stringResource(R.string.lang_en),
                selected = language == "en",
                onClick = { onLanguage("en") },
            )
            ThemeChip(
                label = stringResource(R.string.lang_ru),
                selected = language == "ru",
                onClick = { onLanguage("ru") },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(
            title = stringResource(R.string.settings_debug),
        ) {
            ThemeChip(
                label = stringResource(R.string.debug_on),
                selected = simulateError,
                onClick = { onSimulateError(true) },
            )
            ThemeChip(
                label = stringResource(R.string.debug_off),
                selected = !simulateError,
                onClick = { onSimulateError(false) },
            )
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = LocalQamqorColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    SelectableChip(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(40.dp),
    )
}
