package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Label
import com.example.ui.theme.SecondaryLabel
import com.example.ui.theme.SystemBlue
import com.example.ui.theme.SystemFill
import com.example.ui.theme.SystemOrange
import com.example.ui.theme.SystemRed

@Composable
fun CountdownRing(
    remainingSeconds: Int,
    period: Int = 30,
    size: Dp = 34.dp,
    strokeWidth: Dp = 2.6.dp,
    showNumberInside: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = (remainingSeconds.toFloat() / period.toFloat()).coerceIn(0f, 1f)

    val targetColor = when {
        remainingSeconds <= 1 -> SystemRed
        remainingSeconds <= 5 -> SystemOrange
        else -> SystemBlue
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "ring_color"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val radius = (this.size.minDimension - strokePx) / 2f
            val topLeft = Offset(
                (this.size.width - radius * 2) / 2f,
                (this.size.height - radius * 2) / 2f
            )
            val arcSize = Size(radius * 2, radius * 2)

            // Background Track: systemFill
            drawArc(
                color = SystemFill,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx)
            )

            // Draining Active Arc: systemBlue / systemOrange / systemRed
            drawArc(
                color = animatedColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        if (showNumberInside) {
            Text(
                text = "$remainingSeconds",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (size >= 40.dp) 13.sp else 10.sp,
                color = if (remainingSeconds <= 5) animatedColor else SecondaryLabel
            )
        }
    }
}
