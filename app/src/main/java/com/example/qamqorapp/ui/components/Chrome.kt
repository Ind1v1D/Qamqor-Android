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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.theme.Dimens

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
            .statusBarsPadding(),
    ) {
        if (navigationIcon != null) {
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

@Composable
fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    val c = com.example.qamqorapp.ui.theme.LocalQamqorColors.current
    Box(modifier = modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val c = com.example.qamqorapp.ui.theme.LocalQamqorColors.current
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = c.pine)
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = com.example.qamqorapp.ui.theme.LocalQamqorColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "⚠️", style = MaterialTheme.typography.displaySmall)
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleMedium,
            color = c.ink,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = c.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(c.pine)
                .clickable(onClick = onRetry)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(R.string.retry),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
