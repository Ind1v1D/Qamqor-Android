package com.example.qamqorapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.qamqorapp.ui.theme.Dimens

/**
 * The pine header used by the feed, favorites and create screens.
 *
 * The background deliberately starts *above* the status bar (edge-to-edge): the
 * status-bar inset is applied to the content, not to the surface, so the colour
 * runs behind the system icons — that is what the 64 dp "App Bar" in the spec
 * table measures, inset excluded.
 *
 * @param subtitle optional second line («Алматы · N активных объявлений»).
 * @param navigationIcon optional leading control; when present the layout becomes
 *   56 dp tall, mirroring the «Новый пост» screen's app bar.
 */
@Composable
fun QamqorTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val c = com.example.qamqorapp.ui.theme.LocalQamqorColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.pine)
            // The real inset, not the spec's 24dp estimate: real devices range from
            // 24 to 44dp. `background` is applied *outside* the padding, so the pine
            // still runs edge to edge behind the system icons.
            .statusBarsPadding(),
    ) {
        if (navigationIcon != null) {
            // 56 dp bar with a leading icon — «Новый пост».
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.CreateAppBar.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                navigationIcon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            // 64 dp brand bar — feed / favorites.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.AppBar.dp)
                    .padding(horizontal = Dimens.ContentPadding.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        content?.invoke()
    }
}

/** 32 dp circular back button that sits on top of the detail photo header. */
@Composable
fun BackButton(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(Dimens.BackButton.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Empty-state block used by the feed and the favorites list: a centred column of
 * emoji + title + body, exactly the shape of the placeholder in the mockup.
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = com.example.qamqorapp.ui.theme.LocalQamqorColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = emoji, style = MaterialTheme.typography.displaySmall)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = c.ink,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = c.inkSoft,
            modifier = Modifier.fillMaxWidth(),
        )
        if (actionLabel != null && onAction != null) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(c.pine)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = actionLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/** A full-screen tinted placeholder (loading / fatal), centred. */
@Composable
fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    val c = com.example.qamqorapp.ui.theme.LocalQamqorColors.current
    Box(modifier = modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
    }
}
