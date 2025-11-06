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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.room.experiences.entities.TimedNote

@Preview(showBackground = true)
@Composable
fun TimedNoteRowPreview(@PreviewParameter(TimedNotePreviewProvider::class) timedNote: TimedNote) {
    TimedNoteRow(
        timedNote = timedNote,
        photoCount = 2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Sat 7:34")
    }
}


@Composable
fun TimedNoteRow(
    timedNote: TimedNote,
    photoCount: Int = 0,
    modifier: Modifier = Modifier,
    timeText: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {
        val strokeWidth = 2.dp

        // �?TimedNote 实体中获�?SubstanceColor
        val substanceColor = remember(timedNote) {
            timedNote.customColor?.let { SubstanceColor.Custom(it) }
                ?: timedNote.color?.let { SubstanceColor.Predefined(it) }
        }

        val color = substanceColor?.toColor()
        Canvas(modifier = Modifier
            .fillMaxHeight()
            .width(strokeWidth)
            .padding(vertical = 4.dp)) {
            if (timedNote.isPartOfTimeline && substanceColor != null) {
                val strokeWidthPx = strokeWidth.toPx()
                drawLine(
                    color = color!!,
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
            // Show note text or photo indicator
            if (timedNote.note.isNotBlank()) {
                Text(text = timedNote.note, style = MaterialTheme.typography.bodyMedium)
            }
            // Show photo count if there are photos
            if (photoCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Photos",
                        modifier = Modifier.width(16.dp).height(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = photoCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Show placeholder for photo-only notes
            if (timedNote.note.isBlank() && photoCount > 0) {
                Text(
                    text = "Photo note",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}