package com.example.qamqorapp.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.ui.components.EmptyState
import com.example.qamqorapp.ui.components.PostCard
import com.example.qamqorapp.ui.components.QamqorTopBar
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * «Избранное».
 *
 * Spec: 64 dp app bar with a counter instead of segments/chips, the *same*
 * PostCard component as the feed, and a centred empty state when the Room query
 * for `isFavorite = 1` comes back with zero rows — which is exactly the case the
 * design concept calls out.
 */
@Composable
fun FavoritesScreen(
    onOpenPost: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = qamqorViewModel<FavoritesViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalQamqorColors.current

    Box(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            QamqorTopBar(
                title = stringResource(R.string.favorites_title),
                subtitle = stringResource(R.string.favorites_count, state.posts.size),
            )

            if (state.posts.isEmpty() && !state.isLoading) {
                EmptyState(
                    emoji = "☆",
                    title = stringResource(R.string.favorites_empty_title),
                    body = stringResource(R.string.favorites_empty_body),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = Dimens.ContentPadding.dp,
                        end = Dimens.ContentPadding.dp,
                        top = Dimens.CardSpacing.dp,
                        bottom = Dimens.CardSpacing.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing.dp),
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onClick = { onOpenPost(post.id) },
                            // Tapping the star here *un*-saves it, which is the
                            // natural gesture on a "saved" list.
                            onToggleFavorite = { viewModel.remove(post) },
                        )
                    }
                }
            }
        }
    }
}
