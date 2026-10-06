package com.example.qamqorapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qamqorapp.R
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.util.RelativeTime

/**
 * The status chip on every card («Потерян» / «Найден» / «Пристройство»).
 *
 * Colour comes from the brand tokens — statuses are told apart by *tint*, never
 * by alarm red, which is the whole point of the concept's palette note.
 */
@Composable
fun StatusTag(status: PostStatus, modifier: Modifier = Modifier) {
    val c = LocalQamqorColors.current
    val background = when (status) {
        PostStatus.LOST -> c.dangerSoft
        PostStatus.FOUND -> c.blueSoft
        PostStatus.ADOPT -> c.amberSoft
    }
    val foreground = when (status) {
        PostStatus.LOST -> c.lostInk
        PostStatus.FOUND -> c.foundInk
        PostStatus.ADOPT -> c.adoptInk
    }

    TagChip(
        text = statusLabel(status),
        background = background,
        foreground = foreground,
        modifier = modifier,
    )
}

/** «Решено» — used in place of the status tag on resolved posts. */
@Composable
fun ResolvedTag(modifier: Modifier = Modifier) {
    val c = LocalQamqorColors.current
    TagChip(
        text = stringResource(R.string.status_solved),
        background = c.amberSoft,
        foreground = c.adoptInk,
        modifier = modifier,
    )
}

@Composable
private fun TagChip(
    text: String,
    background: androidx.compose.ui.graphics.Color,
    foreground: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(text = text, color = foreground, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * 56×56 illustration tile with the status tint behind it.
 *
 * `emoji` stands in for what would be a real photo — the concept's placeholder
 * chosen for this build.
 */
@Composable
fun EmojiThumb(post: Post, size: Int, modifier: Modifier = Modifier) {
    val c = LocalQamqorColors.current
    val context = LocalContext.current
    val background = when (post.status) {
        PostStatus.LOST -> c.dangerSoft
        PostStatus.FOUND -> c.blueSoft
        PostStatus.ADOPT -> c.amberSoft
    }
    val emoji = post.emoji.ifBlank { "🐾" }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            // The emoji itself is decorative; TalkBack reads this instead.
            .semantics { contentDescription = context.getString(R.string.cd_post_emoji) },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (size / 2.6f).sp)
    }
}

/**
 * The feed / favorites / profile list row — 76 dp tall, 16 dp radius, hairline
 * border, exactly as the spec table calls for.
 *
 * Reused verbatim on all three screens, which is what makes «Избранное» a one-line
 * screen instead of a second card implementation.
 */
@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.CardHeight.dp)
            .clip(RoundedCornerShape(Dimens.CardRadius.dp))
            .background(colors.panel)
            .border(1.dp, colors.line, RoundedCornerShape(Dimens.CardRadius.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EmojiThumb(post, size = Dimens.CardThumb)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
            if (post.isResolved) ResolvedTag() else StatusTag(post.status)
            Text(
                text = post.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = districtAndTime(post),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        BookmarkButton(
            isFavorite = post.isFavorite,
            onClick = onToggleFavorite,
        )
    }
}

/** The ☆ / ★ in the card corner. */
@Composable
fun BookmarkButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    val context = LocalContext.current
    val cd = context.getString(
        if (isFavorite) R.string.cd_remove_bookmark else R.string.cd_bookmark,
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.Center,
    ) {
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = colors.amber,
                modifier = Modifier.size(20.dp),
            )
        } else {
            StarOutlineIcon(tint = colors.inkSoft, modifier = Modifier.size(20.dp))
        }
    }
}

/** «Алмалинский р-н · вчера» — the card's metadata line. */
@Composable
private fun districtAndTime(post: Post): String {
    val context = LocalContext.current
    val district = context.getString(post.district.labelRes)
    val time = RelativeTime.format(context, post.createdAt)
    return if (time.isEmpty()) district else "$district · $time"
}
