package com.example.qamqorapp.ui.facts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qamqorapp.R
import com.example.qamqorapp.domain.model.PetFact
import com.example.qamqorapp.ui.components.ErrorState
import com.example.qamqorapp.ui.components.LoadingState
import com.example.qamqorapp.ui.components.QamqorTopBar
import org.koin.androidx.compose.koinViewModel
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
@Composable
fun PetFactsRoute(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<PetFactsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PetFactsScreen(
        uiState = uiState,
        onRetry = viewModel::loadFacts,
        modifier = modifier,
    )
}
@Composable
fun PetFactsScreen(
    uiState: PetFactsUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        QamqorTopBar(
            title = stringResource(R.string.facts_title),
            subtitle = stringResource(R.string.facts_subtitle),
        )
        when (uiState) {
            PetFactsUiState.Loading -> LoadingState(modifier = Modifier.weight(1f))

            is PetFactsUiState.Success -> LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(Dimens.ContentPadding.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing.dp),
            ) {
                items(uiState.facts) { fact ->
                    PetFactCard(fact)
                }
            }
            is PetFactsUiState.Error -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(
                    message = uiState.message,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun PetFactCard(fact: PetFact) {
    val colors = LocalQamqorColors.current
    val shape = RoundedCornerShape(Dimens.CardRadius.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.panel)
            .border(1.dp, colors.line, shape)
            .padding(Dimens.ContentPadding.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = fact.emoji,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = fact.text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
    }
}
