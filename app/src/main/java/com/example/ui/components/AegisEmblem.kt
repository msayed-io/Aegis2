package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SystemBlue

@Composable
fun AegisEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = SystemBlue,
    strokeWidth: Float = 3.5f,
    showOuterRing: Boolean = false
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        if (showOuterRing) {
            drawCircle(
                color = tint.copy(alpha = 0.12f),
                radius = cx * 0.94f,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidth * 0.7f)
            )
        }

        // Diamond Rhombus Geometry
        val path = Path().apply {
            moveTo(cx, h * 0.08f)
            lineTo(w * 0.92f, cy)
            lineTo(cx, h * 0.92f)
            lineTo(w * 0.08f, cy)
            close()
        }

        // Subtle dark translucent fill
        drawPath(
            path = path,
            color = tint.copy(alpha = 0.08f),
            style = Fill
        )

        // Diamond contour stroke
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth)
        )

        // Horizontal midline
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
        drawLine(
            color = tint.copy(alpha = 0.9f),
            start = Offset(w * 0.20f, cy),
            end = Offset(w * 0.80f, cy),
            strokeWidth = strokeWidth * 0.85f,
            pathEffect = dashEffect
        )

        // Vertical fine midline
        drawLine(
            color = tint.copy(alpha = 0.45f),
            start = Offset(cx, h * 0.20f),
            end = Offset(cx, h * 0.78f),
            strokeWidth = strokeWidth * 0.65f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        // Center optical node
        drawCircle(
            color = tint,
            radius = strokeWidth * 1.5f,
            center = Offset(cx, cy)
        )
    }
}
