package com.isaakhanimann.journal.ui.tabs.journal.experience.components

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
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.CumulativeDose
import com.isaakhanimann.journal.ui.tabs.journal.experience.models.CumulativeRouteAndDose

@Composable
fun CumulativeDoseRow(
    cumulativeDose: CumulativeDose,
    areDosageDotsHidden: Boolean,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = cumulativeDose.substanceName,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                cumulativeDose.cumulativeRouteAndDose.forEach { cumulativeRouteAndDose ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val text = buildAnnotatedString {
                            append(cumulativeRouteAndDose.doseDescription)
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append(" ${cumulativeRouteAndDose.route.displayText.lowercase()}")
                            }
                        }
                        Text(text = text, style = MaterialTheme.typography.bodyMedium)
                        val numDots = cumulativeRouteAndDose.numDots
                        if (numDots != null && !areDosageDotsHidden) {
                            DotRows(numDots = numDots)
                        }
                    }
                }
            }
        },
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Preview(showBackground = true)
@Composable
fun CumulativeDoseRowPreview() {
    CumulativeDoseRow(
        cumulativeDose = CumulativeDose(
            substanceName = "Amphetamine",
            cumulativeRouteAndDose = listOf(
                CumulativeRouteAndDose(
                    cumulativeDose = 30.0,
                    units = "mg",
                    isEstimate = false,
                    cumulativeDoseStandardDeviation = 12.0,
                    numDots = 6,
                    route = AdministrationRoute.INSUFFLATED,
                    hasMoreThanOneIngestion = true
                ),
                CumulativeRouteAndDose(
                    cumulativeDose = 25.0,
                    units = "mg",
                    isEstimate = false,
                    cumulativeDoseStandardDeviation = 12.0,
                    numDots = 5,
                    route = AdministrationRoute.ORAL,
                    hasMoreThanOneIngestion = true
                )
            )
        ),
        areDosageDotsHidden = false,
        modifier = Modifier.fillMaxWidth()
    )
}