package com.isaakhanimann.journal.ui.tabs.journal.experience.components.timednote

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote

@Preview(showBackground = true)
@Composable
fun TimedNoteRowPreview(@PreviewParameter(TimedNotePreviewProvider::class) timedNote: TimedNote) {
    TimedNoteRow(
        timedNote = timedNote,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Sat 7:34")
    }
}


@Composable
fun TimedNoteRow(
    timedNote: TimedNote,
    modifier: Modifier = Modifier,
    timeText: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {
        val isDarkTheme = isSystemInDarkTheme()
        val strokeWidth = 2.dp
        Canvas(modifier = Modifier
            .fillMaxHeight()
            .width(strokeWidth)
            .padding(vertical = 4.dp)) {
            if (timedNote.isPartOfTimeline) {
                val strokeWidthPx = strokeWidth.toPx()
                drawLine(
                    color = timedNote.color.getComposeColor(isDarkTheme),
                    start = Offset(x = center.x, y = 0f),
                    end = Offset(x = center.x, y = size.height),
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokeWidthPx * 2, strokeWidthPx * 3))
                )
            }
        }
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                timeText()
                if (!timedNote.isPartOfTimeline) {
                    Text(text = "(Not in timeline)", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = timedNote.note, style = MaterialTheme.typography.bodyMedium)
        }
    }
}