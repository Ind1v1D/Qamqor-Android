package com.example.qamqorapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * `border-style: dashed` — which the Compose
 * [androidx.compose.foundation.border] modifier simply does not have. The concept
 * draws the "add photo" placeholder with `border: 1.5px dashed var(--line)` and a
 * `dash(6 5)` pattern, so the stroke is laid out here with
 * [PathEffect.dashPathEffect] instead.
 *
 * @param radius corner radius of the rectangle. The photo box in the spec is square
 *   (0dp); the rounded variant is kept so the helper can be reused elsewhere.
 */
@Stable
fun Modifier.dashedBorder(
    color: Color,
    width: Dp,
    radius: Dp = 0.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 5.dp,
): Modifier = drawBehind {
    val strokeWidth = width.toPx()
    val effect = PathEffect.dashPathEffect(
        floatArrayOf(dashLength.toPx(), gapLength.toPx()),
        phase = 0f,
    )
    // Centre the stroke on the outline, then nudge it inwards by half its own
    // width so the dashes sit exactly on the edge instead of half outside it.
    val inset = strokeWidth / 2f

    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius((radius - inset.toDp()).coerceAtLeast(0.dp).toPx()),
        style = Stroke(width = strokeWidth, pathEffect = effect),
    )
}
