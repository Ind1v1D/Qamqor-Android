package com.example.qamqorapp.ui.feed

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import com.example.qamqorapp.ui.theme.QamqorTheme
private val previewPosts = listOf(
    Post(
        id = 1,
        status = PostStatus.LOST,
        species = Species.DOG,
        name = "Rex, shepherd mix",
        breed = "Shepherd mix",
        district = District.ALMALINSKY,
        address = "Satpayev St.",
        marks = "Red collar",
        description = "Spooked by the fireworks and ran out of our yard.",
        emoji = "🐕",
        author = "Dauren S.",
        isFavorite = true,
    ),
    Post(
        id = 2,
        status = PostStatus.FOUND,
        species = Species.CAT,
        name = "Ginger cat, no collar",
        breed = "Domestic shorthair",
        district = District.BOSTANDYK,
        address = "52 Abay Ave.",
        marks = "White spot on the forehead",
        description = "Has sat by our entrance for three days and is friendly.",
        emoji = "🐈",
        author = "Madina Zh.",
    ),
    Post(
        id = 3,
        status = PostStatus.ADOPT,
        species = Species.CAT,
        name = "Murka, 1 year, spayed",
        breed = "Domestic shorthair",
        district = District.BOSTANDYK,
        address = "Dos shelter",
        marks = "Grey tabby coat",
        description = "Litter-trained, calm and vaccinated.",
        emoji = "🐾",
        author = "Aigerim K.",
    ),
)

@Composable
private fun PreviewFeed(uiState: FeedUiState) {
    QamqorTheme {
        FeedScreen(
            uiState = uiState,
            onQueryChange = {},
            onStatusSelected = {},
            onSpeciesClick = {},
            onDistrictSelected = {},
            onClearFilters = {},
            onFavouriteClick = {},
            onRetry = {},
            onOpenPost = {},
            onCreatePost = {},
        )
    }
}

@Preview(name = "Feed · loading", showBackground = true)
@Composable
private fun FeedLoadingPreview() = PreviewFeed(FeedUiState(isLoading = true))

@Preview(name = "Feed · content", showBackground = true)
@Composable
private fun FeedContentPreview() =
    PreviewFeed(FeedUiState(posts = previewPosts, activeCount = 12, isLoading = false))

@Preview(name = "Feed · empty", showBackground = true)
@Composable
private fun FeedEmptyPreview() = PreviewFeed(
    FeedUiState(
        posts = emptyList(),
        isLoading = false,
        filters = FeedFilters(status = PostStatus.LOST),
    )
)

@Preview(name = "Feed · error (dark)", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FeedErrorPreview() = PreviewFeed(
    FeedUiState(
        posts = emptyList(),
        isLoading = false,
        errorMessage = "Couldn't reach the server. Check your connection.",
    )
)
