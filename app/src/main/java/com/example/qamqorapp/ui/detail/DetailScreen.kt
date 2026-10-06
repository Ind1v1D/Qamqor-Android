package com.example.qamqorapp.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.ui.components.BackButton
import com.example.qamqorapp.ui.components.CenteredMessage
import com.example.qamqorapp.ui.components.EmojiThumb
import com.example.qamqorapp.ui.components.ResolvedTag
import com.example.qamqorapp.ui.components.StarOutlineIcon
import com.example.qamqorapp.ui.components.statusLabel
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.util.RelativeTime

/**
 * «Карточка питомца».
 *
 * Layout per the spec table:
 *   220 dp photo header (pine -> pine-2 gradient, back button 32 dp over it)
 *   scrollable body: name, «видели вчера, 18:40», 2×2 info grid, description
 *   fixed 52 dp action row pinned to the bottom: ☆ «В избранное» + ☎ «Позвонить»
 *
 * The header grows by the status-bar inset so the gradient runs behind the system
 * icons, while the back button is pushed below them.
 */
@Composable
fun DetailScreen(
    postId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<DetailViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Feed the id in once; afterwards the ViewModel owns the request.
    LaunchedEffect(postId) { viewModel.load(postId) }

    val colors = LocalQamqorColors.current

    when {
        state.isLoading -> {
            Box(modifier = modifier.fillMaxSize().background(colors.panel))
        }

        state.missing -> {
            CenteredMessage(
                text = stringResource(R.string.detail_not_found),
                modifier = modifier,
            )
        }

        else -> {
            val post = state.post ?: return
            DetailContent(
                post = post,
                author = state.author,
                onBack = onBack,
                onToggleFavorite = viewModel::toggleFavorite,
                onDeleted = onDeleted,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun DetailContent(
    post: Post,
    author: String,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier,
) {
    val colors = LocalQamqorColors.current
    val context = LocalContext.current
    val statusBars = WindowInsets.statusBars.asPaddingValues()

    Column(modifier = modifier.fillMaxSize().background(colors.panel)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(colors.pine, colors.pine2))
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBars.calculateTopPadding())
                    .height(Dimens.PhotoHeader.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Placeholder for the real photo — the concept's emoji.
                Text(
                    text = post.emoji.ifBlank { "🐾" },
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 52.sp),
                )

                Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
                    BackButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.detail_back),
                    )
                }
            }
        }

        // ---- scrollable body --------------------------------------------------
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ContentPadding.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = post.name,
                style = MaterialTheme.typography.titleLarge,
                color = colors.ink,
            )
            Text(
                text = seenLine(post),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2 × 2 info grid, 64 dp cells, 8 dp gaps, 10 dp radius.
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.InfoGridGap.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                InfoCell(
                    label = stringResource(R.string.detail_field_breed),
                    value = post.breed,
                    modifier = Modifier.weight(1f),
                )
                InfoCell(
                    label = stringResource(R.string.detail_field_status),
                    value = statusLabel(post.status),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(Dimens.InfoGridGap.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.InfoGridGap.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                InfoCell(
                    label = stringResource(R.string.detail_field_marks),
                    value = post.marks,
                    modifier = Modifier.weight(1f),
                )
                InfoCell(
                    label = stringResource(R.string.detail_field_author),
                    value = author,
                    modifier = Modifier.weight(1f),
                )
            }

            if (post.isResolved) {
                Spacer(modifier = Modifier.height(14.dp))
                ResolvedTag()
            }

            // `.detail-body .desc` — a plain paragraph, no heading, in ink-soft.
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = post.description.ifBlank {
                    stringResource(R.string.detail_no_description)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ---- fixed action row, 52 dp, above the navigation bar ---------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.panel)
                .navigationBarsPadding()
                .padding(
                    start = Dimens.ContentPadding.dp,
                    end = Dimens.ContentPadding.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                )
                .height(Dimens.ActionButton.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ActionButtonGap.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionButton(
                label = if (post.isFavorite) {
                    stringResource(R.string.detail_unfavorite)
                } else {
                    stringResource(R.string.detail_favorite)
                },
                leading = {
                    if (post.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = colors.amber,
                        )
                    } else {
                        StarOutlineIcon(
                            tint = colors.amber,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                primary = false,
                onClick = onToggleFavorite,
                modifier = Modifier.weight(1f),
            )
            ActionButton(
                label = stringResource(R.string.detail_call),
                leading = {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = Color.White,
                    )
                },
                primary = true,
                // ACTION_DIAL puts the number in the dialer but never places the
                // call — the safe default for a "call" button in a listing app.
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${post.phone}"))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** «Алмалинский р-н, ул. Сатпаева · видели вчера, 18:40» */
@Composable
private fun seenLine(post: Post): String {
    val context = LocalContext.current
    val district = context.getString(post.district.labelRes)
    val location = if (post.address.isBlank()) district else "$district, ${post.address}"
    val seenAt = post.lastSeenAt ?: post.createdAt
    val seen = RelativeTime.formatWithClock(context, seenAt)
    return "$location · " + context.getString(R.string.detail_seen, seen)
}

/** One cell of the 2×2 grid. */
@Composable
private fun InfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    Column(
        modifier = modifier
            .height(Dimens.InfoCellHeight.dp)
            .clip(RoundedCornerShape(Dimens.InfoCellRadius.dp))
            .background(colors.bg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 52 dp pill: ghost when secondary, pine-filled when primary. */
@Composable
private fun ActionButton(
    label: String,
    leading: @Composable () -> Unit,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .height(Dimens.ActionButton.dp)
            .clip(shape)
            .background(if (primary) colors.pine else colors.panel)
            // A hairline border only on the ghost variant; the filled one needs none.
            .border(
                width = if (primary) 0.dp else 1.dp,
                color = if (primary) Color.Transparent else colors.line,
                shape = shape,
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (primary) Color.White else colors.ink,
            textAlign = TextAlign.Center,
        )
    }
}
