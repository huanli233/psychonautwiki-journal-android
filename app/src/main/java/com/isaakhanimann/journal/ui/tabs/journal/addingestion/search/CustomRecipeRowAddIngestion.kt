package com.isaakhanimann.journal.ui.tabs.journal.addingestion.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import com.isaakhanimann.journal.data.substances.AdministrationRoute

@Composable
fun CustomRecipeRowAddIngestion(
    customRecipeWithSubcomponents: CustomRecipeWithSubcomponents,
    customUnitsMap: Map<Int, CustomUnit>,
    navigateToChooseDoseCustomRecipe: (customRecipeId: Int) -> Unit
) {
    val recipe = customRecipeWithSubcomponents.recipe

    ListItem(
        modifier = Modifier.clickable {
            navigateToChooseDoseCustomRecipe(recipe.id)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = recipe.administrationRoute.displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.recipe_contains, customRecipeWithSubcomponents.getSubcomponentsSummary(customUnitsMap)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (recipe.note.isNotBlank()) {
                    Text(
                        text = recipe.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun CustomRecipeRowAddIngestionPreview() {
    val sampleRecipe = CustomRecipe.sampleRecipe
    val sampleSubcomponent = RecipeSubcomponent.sampleSubcomponent
    val sampleRecipeWithSubcomponents = CustomRecipeWithSubcomponents(
        recipe = sampleRecipe,
        subcomponents = listOf(sampleSubcomponent)
    )

    CustomRecipeRowAddIngestion(
        customRecipeWithSubcomponents = sampleRecipeWithSubcomponents,
        customUnitsMap = emptyMap(),
        navigateToChooseDoseCustomRecipe = {}
    )
}