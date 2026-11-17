package com.example.qamqorapp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import com.example.qamqorapp.ui.components.EmptyState
import com.example.qamqorapp.ui.components.ErrorState
import com.example.qamqorapp.ui.components.FeedFilterRow
import com.example.qamqorapp.ui.components.LoadingState
import com.example.qamqorapp.ui.components.PostCard
import com.example.qamqorapp.ui.components.QamqorField
import com.example.qamqorapp.ui.components.QamqorTopBar
import com.example.qamqorapp.ui.components.StatusSegments
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
@Composable
fun FeedRoute(
    onOpenPost: (Long) -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<FeedViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeedScreen(
        uiState = state,
        onQueryChange = viewModel::onQueryChange,
        onStatusSelected = viewModel::selectStatus,
        onSpeciesClick = viewModel::toggleSpecies,
        onDistrictSelected = viewModel::selectDistrict,
        onClearFilters = viewModel::clearFilters,
        onFavouriteClick = viewModel::onFavouriteClick,
        onRetry = viewModel::onRetry,
        onOpenPost = onOpenPost,
        onCreatePost = onCreatePost,
        modifier = modifier,
    )
}
@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onQueryChange: (String) -> Unit,
    onStatusSelected: (PostStatus?) -> Unit,
    onSpeciesClick: (Species) -> Unit,
    onDistrictSelected: (District?) -> Unit,
    onClearFilters: () -> Unit,
    onFavouriteClick: (Post) -> Unit,
    onRetry: () -> Unit,
    onOpenPost: (Long) -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    val context = LocalContext.current

    val subtitle = stringResource(
        R.string.feed_subtitle,
        stringResource(R.string.feed_city),
        context.resources.getQuantityString(
            R.plurals.posts_count,
            uiState.activeCount,
            uiState.activeCount,
        ),
    )

    Box(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            QamqorTopBar(
                title = stringResource(R.string.feed_brand),
                subtitle = subtitle,
            ) {
                Column(modifier = Modifier.padding(
                    start = Dimens.ContentPadding.dp,
                    end = Dimens.ContentPadding.dp,
                    bottom = Dimens.SegmentsTopSpacing.dp,
                )) {
                    StatusSegments(
                        selected = uiState.filters.status,
                        onSelect = onStatusSelected,
                    )
                }
            }

            SearchField(
                value = uiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier.padding(
                    horizontal = Dimens.ContentPadding.dp,
                    vertical = 8.dp,
                ),
            )

            FeedFilterRow(
                species = uiState.filters.species,
                district = uiState.filters.district,
                onSpecies = onSpeciesClick,
                onDistrict = onDistrictSelected,
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.isLoading -> LoadingState()

                    uiState.hasError -> ErrorState(
                        message = uiState.errorMessage ?: stringResource(R.string.error_generic),
                        onRetry = onRetry,
                    )

                    uiState.isEmpty -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        item {
                            EmptyState(
                                emoji = "🐾",
                                title = stringResource(R.string.feed_empty_title),
                                body = stringResource(R.string.feed_empty_body),
                                actionLabel = if (uiState.isFiltered) {
                                    stringResource(R.string.feed_retry)
                                } else {
                                    null
                                },
                                onAction = if (uiState.isFiltered) onClearFilters else null,
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Dimens.ContentPadding.dp,
                            end = Dimens.ContentPadding.dp,
                            top = Dimens.CardSpacing.dp,
                            bottom = Dimens.FabSize.dp + Dimens.ContentPadding.dp * 2,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing.dp),
                    ) {
                        items(uiState.posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                onClick = { onOpenPost(post.id) },
                                onToggleFavorite = { onFavouriteClick(post) },
                            )
                        }
                    }
                }
            }
        }
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

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    QamqorField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = stringResource(R.string.feed_search_hint),
        containerColor = colors.panel,
        leading = {
            Text(text = "🔍", style = MaterialTheme.typography.bodyMedium)
        },
        trailing = {
            if (value.isNotEmpty()) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSoft,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onValueChange("") }
                        .padding(4.dp),
                )
            }
        },
    )
}
