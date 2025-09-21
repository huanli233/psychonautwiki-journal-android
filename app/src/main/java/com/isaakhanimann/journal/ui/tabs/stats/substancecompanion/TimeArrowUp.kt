package com.isaakhanimann.journal.ui.tabs.stats.substancecompanion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TimeArrowUp(timeText: String) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val strokeWidth = 2.dp
    val arrowHeadSize = 6.dp
    val lineHeight = 24.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(arrowHeadSize * 2, lineHeight)) {
            val strokeWidthPx = strokeWidth.toPx()
            val headSizePx = arrowHeadSize.toPx()

            drawLine(
                color = color,
                start = Offset(center.x, headSizePx / 2),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )

            val path = Path().apply {
                moveTo(center.x, 0f)
                lineTo(center.x - headSizePx / 2, headSizePx / 2) // 左翼
                moveTo(center.x, 0f)
                lineTo(center.x + headSizePx / 2, headSizePx / 2) // 右翼
            }
            drawPath(path, color, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round))
        }

        Text(
            text = timeText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Canvas(modifier = Modifier.height(lineHeight).width(strokeWidth)) {
            drawLine(
                color = color,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun TimeArrowUpPreview() {
    TimeArrowUp(timeText = "4.5 hours")
}