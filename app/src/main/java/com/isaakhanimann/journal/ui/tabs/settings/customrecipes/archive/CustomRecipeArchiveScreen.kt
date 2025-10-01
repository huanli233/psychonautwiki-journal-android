/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.settings.customrecipes.archive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.entities.CustomRecipe
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.room.experiences.entities.RecipeSubcomponent
import com.isaakhanimann.journal.data.room.experiences.relations.CustomRecipeWithSubcomponents
import com.isaakhanimann.journal.ui.tabs.settings.customrecipes.CustomRecipeRow
import com.isaakhanimann.journal.ui.tabs.stats.EmptyScreenDisclaimer

@Composable
fun CustomRecipeArchiveScreen(
    viewModel: CustomRecipeArchiveViewModel = hiltViewModel(),
    navigateToEditCustomRecipe: (customRecipeId: Int) -> Unit,
) {
    val recipesWithUnits = viewModel.recipesWithUnitsFlow.collectAsState().value
    
    CustomRecipeArchiveScreenContent(
        customRecipes = recipesWithUnits.recipes,
        customUnitsMap = recipesWithUnits.customUnitsMap,
        navigateToEditCustomRecipe = navigateToEditCustomRecipe,
    )
}

@Preview
@Composable
fun CustomRecipeArchiveScreenPreview() {
    val sampleRecipe = CustomRecipe.sampleRecipe.copy(isArchived = true)
    val sampleSubcomponent = RecipeSubcomponent.sampleSubcomponent
    val sampleRecipeWithSubcomponents = CustomRecipeWithSubcomponents(
        recipe = sampleRecipe,
        subcomponents = listOf(sampleSubcomponent)
    )
    
    CustomRecipeArchiveScreenContent(
        customRecipes = listOf(sampleRecipeWithSubcomponents),
        customUnitsMap = emptyMap(),
        navigateToEditCustomRecipe = { _ -> },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRecipeArchiveScreenContent(
    customRecipes: List<CustomRecipeWithSubcomponents>,
    customUnitsMap: Map<Int, CustomUnit>,
    navigateToEditCustomRecipe: (customRecipeId: Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.custom_recipe_archive_title)) })
        }
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(customRecipes, key = { it.recipe.id }) { customRecipeWithSubcomponents ->
                    CustomRecipeRow(
                        customRecipeWithSubcomponents = customRecipeWithSubcomponents,
                        customUnitsMap = customUnitsMap,
                        navigateToEditCustomRecipe = navigateToEditCustomRecipe
                    )
                    HorizontalDivider()
                }
            }
            if (customRecipes.isEmpty()) {
                EmptyScreenDisclaimer(
                    title = stringResource(R.string.no_archived_recipes_yet),
                    description = stringResource(R.string.no_archived_recipes_description)
                )
            }
        }
    }
}