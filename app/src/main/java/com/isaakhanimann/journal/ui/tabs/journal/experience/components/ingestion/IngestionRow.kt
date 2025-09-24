package com.isaakhanimann.journal.ui.tabs.journal.experience.components.ingestion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.SubstanceColor
import com.isaakhanimann.journal.data.room.experiences.entities.getSubstanceColor
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VerticalLine(color = ingestionWithCompanionAndCustomUnit.substanceCompanion?.getSubstanceColor() ?: SubstanceColor.Predefined(AdaptiveColor.RED))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val title = if (customUnit != null) {
                "${ingestion.substanceName}, ${customUnit.name}"
            } else {
                ingestion.substanceName
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

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

            val note = ingestion.notes
            if (!note.isNullOrBlank()) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val numDots = ingestionElement.numDots
        if (numDots != null && !areDosageDotsHidden) {
            DotRows(numDots = numDots)
        }

        Column(modifier = Modifier.widthIn(min = 60.dp), horizontalAlignment = Alignment.End) {
            time()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun IngestionRowPreview(@PreviewParameter(IngestionRowPreviewProvider::class) ingestionElement: IngestionElement) {

    Column {
        IngestionRow(
            ingestionElement = ingestionElement,
            areDosageDotsHidden = false,
        ) {
            Text(
                text = "07:17",
                style = MaterialTheme.typography.labelMedium
            )
        }
        IngestionRow(
            ingestionElement = ingestionElement,
            areDosageDotsHidden = false,
        ) {
            Text(
                text = "Fri 07:17",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}