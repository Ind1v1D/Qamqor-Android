package com.example.qamqorapp.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.ui.theme.QamqorTheme

@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQamqorColors.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = Dimens.CategoryChip.dp, minWidth = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.pine else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.pine else colors.line,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else colors.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Chip unselected", showBackground = true)
@Preview(name = "Chip selected", showBackground = true)
@Composable
private fun SelectableChipPreview() {
    QamqorTheme {
        SelectableChip(label = "Dogs", selected = false, onClick = {})
    }
}

@Preview(name = "Chip selected dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SelectableChipPreviewDark() {
    QamqorTheme(darkTheme = true) {
        SelectableChip(label = "Dogs", selected = true, onClick = {})
    }
}
