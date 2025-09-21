package com.isaakhanimann.journal.ui.tabs.journal.addingestion.search

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit

@Composable
fun CustomUnitRowAddIngestion(
    customUnit: CustomUnit,
    navigateToCustomUnitChooseDose: (customUnitId: Int) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = "${customUnit.substanceName} ${customUnit.administrationRoute.displayText.lowercase()}, ${customUnit.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = "${customUnit.getDoseOfOneUnitDescription()} per ${customUnit.unit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable { navigateToCustomUnitChooseDose(customUnit.id) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
