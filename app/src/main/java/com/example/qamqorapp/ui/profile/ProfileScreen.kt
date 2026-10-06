package com.example.qamqorapp.ui.profile

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.ui.components.EmojiThumb
import com.example.qamqorapp.ui.components.ResolvedTag
import com.example.qamqorapp.ui.components.StatusTag
import com.example.qamqorapp.ui.dialogs.ConfirmDialog
import com.example.qamqorapp.ui.dialogs.EditProfileDialog
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.util.RelativeTime

/**
 * «Профиль».
 *
 * Layout per the spec table: header (64 dp avatar, name, phone, tap to edit), a
 * 52 dp three-column stats strip with hairlines, a 28 dp section title, 58 dp post
 * rows with ✓ and 🗑, and a 44 dp outlined «Выйти».
 *
 * All three counters are live Room queries, so resolving a post anywhere updates
 * «Решено» without a manual refresh.
 */
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenPost: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<ProfileViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalQamqorColors.current

    var showEdit by remember { mutableStateOf(false) }
    var showSignOut by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ---- header ------------------------------------------------------
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
                        .background(colors.amberSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = colors.adoptInk,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.profile?.name ?: "—",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Text(
                    text = state.profile?.phone ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSoft,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.profile_edit),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.pine,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEdit = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )

                // ---- stats strip: hairlines top and bottom -------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.StatsRow.dp)
                        .padding(top = 10.dp, bottom = 10.dp)
                        .border(1.dp, colors.line),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatCell(
                        value = state.published,
                        label = stringResource(R.string.stat_published),
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider()
                    StatCell(
                        value = state.resolved,
                        label = stringResource(R.string.stat_solved),
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider()
                    StatCell(
                        value = state.favorites,
                        label = stringResource(R.string.stat_favorites),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ---- «Мои посты» -------------------------------------------------
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

            if (state.myPosts.isEmpty() && !state.isLoading) {
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
                    state.myPosts.forEach { post ->
                        MyPostRow(
                            post = post,
                            onClick = { onOpenPost(post.id) },
                            onToggleResolved = { viewModel.toggleResolved(post) },
                            onDelete = { viewModel.delete(post) },
                        )
                    }
                }
            }

            // ---- «Выйти» ------------------------------------------------------
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
            initialName = state.profile?.name.orEmpty(),
            initialPhone = state.profile?.phone.orEmpty(),
            onDismiss = { showEdit = false },
            onSave = { name, phone ->
                viewModel.saveProfile(name, phone)
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
                // The shell observes the profile going null and returns to setup.
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

/**
 * 58 dp row from the «Мои посты» mockup: 40 dp preview, status tag, name, meta and
 * two icon buttons (✓ resolved, 🗑 delete). Resolved rows are dimmed, matching the
 * third row of the design.
 */
@Composable
private fun MyPostRow(
    post: Post,
    onClick: () -> Unit,
    onToggleResolved: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalQamqorColors.current
    val context = LocalContext.current

    // Resolved posts render at reduced opacity, exactly like the mockup.
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

/** 26 dp outlined icon button used for the ✓ / 🗑 actions. */
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
            // The whole box is the tap target; TalkBack reads the action's name.
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
    }
}
