package com.example.qamqorapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * The ☆ bookmark glyph.
 *
 * `material-icons-core` has no star *outline* — `Icons.Outlined.Star` is still a
 * solid star (Material only hollows out shapes whose filled and outlined forms
 * differ, and the star is not one of them). So the 5-point outline the design
 * concept uses is drawn here instead: a regular star polygon stroked rather than
 * filled, at the same optical size as an icon font glyph.
 *
 * @param tint stroke colour — the inactive ink-soft, or amber when saved.
 */
@Composable
fun StarOutlineIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 1.7f,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        // Leave a hair of margin so the stroke is not clipped at the bounds.
        val outer = size.minDimension / 2f - strokeWidth
        val inner = outer * 0.42f

        val path = Path()
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) outer else inner
            // -90° puts the first point at the top, 36° per point alternates
            // tip / valley around the circle.
            val angle = Math.toRadians((-90 + i * 36).toDouble())
            val point = Offset(
                x = cx + (radius * cos(angle)).toFloat(),
                y = cy + (radius * sin(angle)).toFloat(),
            )
            if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        path.close()

        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
