package com.example.qamqorapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.qamqorapp.R
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors

/**
 * The four-way status segment row inside the pine app bar
 * («Все / Потеряно / Найдено / Пристройство»).
 *
 * Rendering it as a plain Row of equal-width boxes rather than Material3's
 * SegmentedButton keeps the mockup's exact look: 36 dp tall, 8 dp radius, the
 * active segment flipping to white-on-pine.
 */
@Composable
fun StatusSegments(
    selected: PostStatus?,
    onSelect: (PostStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalQamqorColors.current

    // The «Все» segment maps to null == "no status filter".
    val entries: List<Pair<String, PostStatus?>> = listOf(
        stringResource(R.string.seg_all) to null,
        stringResource(R.string.seg_lost) to PostStatus.LOST,
        stringResource(R.string.seg_found) to PostStatus.FOUND,
        stringResource(R.string.seg_adopt) to PostStatus.ADOPT,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.Segment.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        entries.forEach { (label, status) ->
            val active = selected == status
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) Color.White else Color.White.copy(alpha = 0.12f))
                    .clickable { onSelect(status) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) c.pine else Color.White,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** A single filter chip; `selected` tints it with the amber-soft token. */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalQamqorColors.current

    Box(
        modifier = modifier
            .height(Dimens.ChipHeight.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) c.amberSoft else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else c.line,
                shape = RoundedCornerShape(100.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) c.adoptInk else c.inkSoft,
            maxLines = 1,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

/**
 * The species + district filter row under the app bar (44 dp, horizontally
 * scrollable, as the spec table requires).
 *
 * No «all districts» chip: the concept shows only the real districts, all of them
 * unselected at rest, and tapping the active district again is what clears it.
 */
@Composable
fun FeedFilterRow(
    species: Species?,
    district: District?,
    onSpecies: (Species) -> Unit,
    onDistrict: (District?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalQamqorColors.current

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.FilterRow.dp)
            .background(c.panel)
            // `.filters{border-bottom:1px solid var(--line)}` separates the chip
            // strip from the feed below it.
            .drawBehind {
                drawLine(
                    color = c.line,
                    start = Offset(0f, size.height - 1.dp.toPx()),
                    end = Offset(size.width, size.height - 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            },
        contentPadding = PaddingValues(
            horizontal = Dimens.ContentPadding.dp,
            vertical = ((Dimens.FilterRow - Dimens.ChipHeight) / 2).dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "species_dog") {
            FilterChip(
                label = stringResource(R.string.chip_dogs),
                selected = species == Species.DOG,
                onClick = { onSpecies(Species.DOG) },
            )
        }
        item(key = "species_cat") {
            FilterChip(
                label = stringResource(R.string.chip_cats),
                selected = species == Species.CAT,
                onClick = { onSpecies(Species.CAT) },
            )
        }

        items(District.entries, key = { it.name }) { d ->
            FilterChip(
                label = stringResource(d.labelRes),
                selected = district == d,
                onClick = { onDistrict(if (district == d) null else d) },
            )
        }
    }
}
