package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.DotRows
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.IngestionElement

@Composable
fun IngestionRow(
    ingestionElement: IngestionElement,
    areDosageDotsHidden: Boolean,
    modifier: Modifier = Modifier,
    time: @Composable () -> Unit,
) {
    val ingestionWithCompanionAndCustomUnit = ingestionElement.ingestionWithCompanionAndCustomUnit
    val ingestion = ingestionWithCompanionAndCustomUnit.ingestion
    val customUnit = ingestionWithCompanionAndCustomUnit.customUnit

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            val title = if (customUnit != null) {
                "${ingestion.substanceName}, ${customUnit.name}"
            } else {
                ingestion.substanceName
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val doseText = buildAnnotatedString {
                        append(ingestionWithCompanionAndCustomUnit.doseDescription)
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            val routeText = " ${ingestion.administrationRoute.displayText.lowercase()}"
                            if (customUnit == null) {
                                append(routeText)
                            } else {
                                val calculatedDose = ingestionWithCompanionAndCustomUnit.customUnitDose?.calculatedDoseDescription
                                val calculatedText = if (calculatedDose != null) " = $calculatedDose" else " = unknown dose"
                                append(calculatedText + routeText)
                            }
                        }
                    }
                    Text(text = doseText, style = MaterialTheme.typography.bodyMedium)

                    val numDots = ingestionElement.numDots
                    if (numDots != null && !areDosageDotsHidden) {
                        DotRows(numDots = numDots)
                    }
                }

                val note = ingestion.notes
                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            VerticalLine(color = ingestionWithCompanionAndCustomUnit.substanceCompanion?.color ?: AdaptiveColor.RED)
        },
        trailingContent = {
            time()
        }
    )
}


@Preview(showBackground = true)
@Composable
fun IngestionRowPreview(@PreviewParameter(IngestionRowPreviewProvider::class) ingestionElement: IngestionElement) {
    IngestionRow(
        ingestionElement = ingestionElement,
        areDosageDotsHidden = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Fri 07:17",
            style = MaterialTheme.typography.labelMedium
        )
    }
}