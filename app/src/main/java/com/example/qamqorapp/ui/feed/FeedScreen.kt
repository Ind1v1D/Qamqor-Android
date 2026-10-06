package com.example.qamqorapp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.components.EmptyState
import com.example.qamqorapp.ui.components.FeedFilterRow
import com.example.qamqorapp.ui.components.PostCard
import com.example.qamqorapp.ui.components.QamqorTopBar
import com.example.qamqorapp.ui.components.StatusSegments
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * «Главная лента».
 *
 * Structure top-to-bottom, matching the spec table:
 *   pine app bar (status-bar inset + 64 dp brand + 36 dp segments)
 *   filter chips row (44 dp)
 *   LazyColumn of 76 dp cards — the *only* scrolling region; the header stays put
 *   56 dp FAB, 16 dp from the right and 16 dp above the bottom navigation
 */
@Composable
fun FeedScreen(
    onOpenPost: (Long) -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<FeedViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalQamqorColors.current
    val context = LocalContext.current

    // «Алматы · 128 активных объявлений» — the count is a live Room query, so the
    // plural is resolved through the quantity API rather than a hardcoded string.
    val subtitle = stringResource(
        R.string.feed_subtitle,
        stringResource(R.string.feed_city),
        context.resources.getQuantityString(
            R.plurals.posts_count,
            state.activeCount,
            state.activeCount,
        ),
    )

    Box(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            QamqorTopBar(
                title = stringResource(R.string.feed_brand),
                subtitle = subtitle,
            ) {
                Box(modifier = Modifier.padding(
                    start = Dimens.ContentPadding.dp,
                    end = Dimens.ContentPadding.dp,
                    bottom = Dimens.SegmentsTopSpacing.dp,
                )) {
                    StatusSegments(
                        selected = state.filters.status,
                        onSelect = viewModel::selectStatus,
                    )
                }
            }

            FeedFilterRow(
                species = state.filters.species,
                district = state.filters.district,
                onSpecies = viewModel::toggleSpecies,
                onDistrict = viewModel::selectDistrict,
            )

            if (state.posts.isEmpty() && !state.isLoading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    item {
                        EmptyState(
                            emoji = "🐾",
                            title = stringResource(R.string.feed_empty_title),
                            body = stringResource(R.string.feed_empty_body),
                            actionLabel = if (state.filters.isActive) {
                                stringResource(R.string.feed_retry)
                            } else {
                                null
                            },
                            onAction = if (state.filters.isActive) {
                                { viewModel.clearFilters() }
                            } else {
                                null
                            },
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ContentPadding.dp,
                        end = Dimens.ContentPadding.dp,
                        top = Dimens.CardSpacing.dp,
                        // Leaves room for the FAB so the last card is not hidden.
                        bottom = Dimens.FabSize.dp + Dimens.ContentPadding.dp * 2,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing.dp),
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onClick = { onOpenPost(post.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(post) },
                        )
                    }
                }
            }
        }

        // FAB: 56×56, 16dp from the right edge and 16dp above the bottom bar.
        // The NavHost is *already* inset by the bottom-bar height (bar + gesture
        // inset), so this screen's frame ends exactly where the tab bar starts —
        // only the 16dp offset from the spec remains to be applied here.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = Dimens.ContentPadding.dp,
                    bottom = Dimens.ContentPadding.dp,
                )
                .size(Dimens.FabSize.dp)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.amber)
                .clickable(onClick = onCreatePost),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
