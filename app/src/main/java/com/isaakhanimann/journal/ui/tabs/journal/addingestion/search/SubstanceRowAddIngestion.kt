package com.isaakhanimann.journal.ui.tabs.journal.addingestion.search

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import com.isaakhanimann.journal.ui.tabs.search.substancerow.SubstanceModelPreviewProvider

@Preview(showBackground = true)
@Composable
fun SubstanceRowAddIngestionPreview(
    @PreviewParameter(SubstanceModelPreviewProvider::class) substanceModel: SubstanceModel
) {
    SubstanceRowAddIngestion(substanceModel = substanceModel, onTap = {})
}

@Composable
fun SubstanceRowAddIngestion(
    substanceModel: SubstanceModel,
    onTap: (substanceName: String) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = substanceModel.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = if (substanceModel.commonNames.isNotEmpty()) {
            {
                val commaSeparatedNames = substanceModel.commonNames.joinToString(separator = ", ")
                Text(text = commaSeparatedNames, style = MaterialTheme.typography.bodyMedium)
            }
        } else null,
        modifier = Modifier.clickable { onTap(substanceModel.name) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}